package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransformationResult;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.TransactionLineConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionPipelineProcessor {

    private final GenesisService genesisService;

    private final AccountingCoreRepository accountingCoreRepository;

    private final BusinessRulesConvertor businessRulesConvertor;

    private final ConversionsService conversionsService;

    private final TransactionLineConverter transactionLineConverter;

    private final CleansingService cleansingService;

    @Transactional
    public TransformationResult runPipeline(TransactionLines transactionLines) {
        val genesisTransactionsResult = genesisService.run(
                transactionLines,
                new TransactionLines(transactionLines.organisationId(), List.of())
        );

        val genesisPassThrough = genesisTransactionsResult.passThroughTransactionLines();
        val genesisFiltered = genesisTransactionsResult.filteredTransactionLines();

        val cleansedTransactionsResult = cleansingService.run(
                genesisPassThrough,
                genesisFiltered
        );

        val cleansedPassThrough = cleansedTransactionsResult.passThroughTransactionLines();
        val cleansedFiltered = cleansedTransactionsResult.ignoredTransactionLines();


        val preValidationBusinessRulesResult = businessRulesConvertor.runPreValidation(
                cleansedPassThrough,
                cleansedFiltered
        );

        val preValidationPassThrough = preValidationBusinessRulesResult.passThroughTransactionLines();
        val preValidationFiltered = preValidationBusinessRulesResult.filteredTransactionLines();

        val convertedTransactionsResult = conversionsService.run(
                preValidationPassThrough,
                preValidationFiltered
        );
        val convertedPassThrough = convertedTransactionsResult.passThroughTransactionLines();
        val convertedFiltered = convertedTransactionsResult.ignoredTransactionLines();

        val postValidationTransactionsResult = businessRulesConvertor.runPostValidation(
                convertedPassThrough,
                convertedFiltered
        );

        syncToDb(postValidationTransactionsResult.passThroughTransactionLines(), postValidationTransactionsResult.filteredTransactionLines());

        return new TransformationResult(
                postValidationTransactionsResult.passThroughTransactionLines(),
                postValidationTransactionsResult.ignoredTransactionLines(),
                postValidationTransactionsResult.filteredTransactionLines(),
                postValidationTransactionsResult.violations()
        );
    }

    @Transactional
    private void syncToDb(TransactionLines passedTxLines,
                          TransactionLines filteredTxLines) {
        val txLineEntities = passedTxLines.entries().stream()
                .map(transactionLineConverter::convert)
                .toList();

        val filteredIds = filteredTxLines.entries()
                .stream()
                .map(TransactionLine::getId)
                .toList();

        accountingCoreRepository.saveAll(txLineEntities);
        accountingCoreRepository.deleteAllById(filteredIds);
    }

}

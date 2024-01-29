package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLine;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
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
    public TransactionLines runPipeline(TransactionLines transactionLines) {
        val genesisTransactionsResult = genesisService.run(
                transactionLines,
                new TransactionLines(transactionLines.organisationId(), List.of())
        );

        if (!genesisTransactionsResult.violations().isEmpty()) {
            syncToDb(genesisTransactionsResult.passThroughTransactionLines(), genesisTransactionsResult.filteredTransactionLines());

            // publish errors via notification gateway

            return genesisTransactionsResult.passThroughTransactionLines();
        }

        val cleansedTransactionsResult = cleansingService.run(
                genesisTransactionsResult.passThroughTransactionLines(),
                genesisTransactionsResult.ignoredTransactionLines()
        );

        if (!cleansedTransactionsResult.violations().isEmpty()) {
            syncToDb(cleansedTransactionsResult.passThroughTransactionLines(), cleansedTransactionsResult.filteredTransactionLines());

            // publish errors via notification gateway

            return cleansedTransactionsResult.passThroughTransactionLines();
        }

        val preValidationBusinessRulesResult = businessRulesConvertor.runPreValidation(
                cleansedTransactionsResult.passThroughTransactionLines(),
                cleansedTransactionsResult.ignoredTransactionLines()
        );

        if (!preValidationBusinessRulesResult.violations().isEmpty()) {
            syncToDb(preValidationBusinessRulesResult.passThroughTransactionLines(), preValidationBusinessRulesResult.filteredTransactionLines());

            // publish errors via notification gateway

            return preValidationBusinessRulesResult.passThroughTransactionLines();
        }

        val convertedTransactionsResult = conversionsService.run(
                preValidationBusinessRulesResult.passThroughTransactionLines(),
                preValidationBusinessRulesResult.ignoredTransactionLines()
        );

        if (!convertedTransactionsResult.violations().isEmpty()) {
            // publish errors via notification gateway

            syncToDb(convertedTransactionsResult.passThroughTransactionLines(), convertedTransactionsResult.filteredTransactionLines());

            return convertedTransactionsResult.passThroughTransactionLines();
        }

        val postValidationTransactionsResult = businessRulesConvertor.runPostValidation(
                convertedTransactionsResult.passThroughTransactionLines(),
                convertedTransactionsResult.ignoredTransactionLines()
        );

        syncToDb(postValidationTransactionsResult.passThroughTransactionLines(), postValidationTransactionsResult.filteredTransactionLines());

        return postValidationTransactionsResult.passThroughTransactionLines();
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

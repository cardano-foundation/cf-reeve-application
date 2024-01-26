package org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionLines;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreRepository;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.TransactionLineConverter;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.BusinessRulesConvertor;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.CleansingService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.ConversionsService;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.pipeline.GenesisService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        val genesisTransactionsResult = genesisService.run(transactionLines);
        if (!genesisTransactionsResult.violations().isEmpty()) {
            storeAll(genesisTransactionsResult.passThroughTransactionLines());

            // publish errors via notification gateway

            return genesisTransactionsResult.passThroughTransactionLines();
        }

        val cleansedTransactionsResult = cleansingService.run(genesisTransactionsResult.passThroughTransactionLines());
        if (!cleansedTransactionsResult.violations().isEmpty()) {
            storeAll(cleansedTransactionsResult.passThroughTransactionLines());

            // publish errors via notification gateway

            return cleansedTransactionsResult.passThroughTransactionLines();
        }

        val preValidationBusinessRulesResult = businessRulesConvertor.runPreValidation(cleansedTransactionsResult.passThroughTransactionLines());
        if (!preValidationBusinessRulesResult.violations().isEmpty()) {

            // publish errors via notification gateway

            return preValidationBusinessRulesResult.passThroughTransactionLines();
        }

        val convertedTransactionsResult = conversionsService.run(preValidationBusinessRulesResult.passThroughTransactionLines());
        if (!convertedTransactionsResult.violations().isEmpty()) {
            // publish errors via notification gateway

            return convertedTransactionsResult.passThroughTransactionLines();
        }

        val postValidationTransactionsResult = businessRulesConvertor.runPostValidation(convertedTransactionsResult.passThroughTransactionLines());
        if (!convertedTransactionsResult.violations().isEmpty()) {
            // publish errors via notification gateway

            return postValidationTransactionsResult.passThroughTransactionLines();
        }

        storeAll(postValidationTransactionsResult.passThroughTransactionLines());

        return postValidationTransactionsResult.passThroughTransactionLines();
    }

    @Transactional
    private void storeAll(TransactionLines transactionLines) {
        val txLineEntities = transactionLines.entries().stream()
                .map(transactionLineConverter::convert)
                .toList();

        accountingCoreRepository.saveAllAndFlush(txLineEntities);
    }

}

package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.accounting_reporting_core.repository.AccountingCoreTransactionRepository;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.*;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.CodesMappingRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionBodyRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.event_handle.NetSuiteEventHandler;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal.*;
import org.cardanofoundation.lob.app.organisation.util.SystemExtractionParametersFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;

import java.time.Clock;
import java.util.HashMap;
import java.util.function.Function;

import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType.*;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "lob.netsuite.enabled", havingValue = "true", matchIfMissing = true)
public class CFConfig {

    private final static String NETSUITE_CONNECTOR_ID = "fEU237r9rqAPEGEFY1yr";

    @Bean
    public NetSuiteClient netSuiteClient(ObjectMapper objectMapper,
                                         @Qualifier("netsuiteRestClient") RestClient restClient,
                                         @Value("${lob.netsuite.client.url}") String url,
                                         @Value("${lob.netsuite.client.token-url}") String tokenUrl,
                                         @Value("${lob.netsuite.client.private-key-file-path}") String privateKeyFilePath,
                                         @Value("${lob.netsuite.client.client-id}") String clientId,
                                         @Value("${lob.netsuite.client.certificate-id}") String certificateId,
                                         @Value("${lob.netsuite.client.recordspercall}") int recordsPerCall
    ) {
        log.info("Creating NetSuite client with url: {}", url);
        return new NetSuiteClient(objectMapper, restClient, url, tokenUrl, privateKeyFilePath, certificateId, clientId, recordsPerCall);
    }

    @Bean
    public NetSuiteParser netSuiteParser(ObjectMapper objectMapper, IngestionRepository ingestionRepository, IngestionBodyRepository ingestionBodyRepository, Clock clock) {
        return new NetSuiteParser(objectMapper, ingestionRepository, ingestionBodyRepository, NETSUITE_CONNECTOR_ID, clock);
    }

    @Bean
    public ExtractionParametersFilteringService extractionParametersFilteringService() {
        return new ExtractionParametersFilteringService();
    }

    @Bean("netsuite_adapter.TransactionConverter")
    public TransactionConverter transactionConverter(Validator validator,
                                                     CodesMappingService codesMappingService,
                                                     PreprocessorService preprocessorService,
                                                     TransactionTypeMapper transactionTypeMapper,
                                                     @Value("${lob.netsuite.financial.period.source:IMPLICIT}") FinancialPeriodSource financialPeriodSource) {
        return new TransactionConverter(validator,
                codesMappingService,
                preprocessorService,
                transactionTypeMapper,
                NETSUITE_CONNECTOR_ID,
                financialPeriodSource
        );
    }

    @Bean
    public NetSuiteExtractionService netSuiteExtractionService(IngestionRepository ingestionRepository,
                                                               NetSuiteClient netSuiteClient,
                                                               TransactionConverter transactionConverter,
                                                               ApplicationEventPublisher eventPublisher,
                                                               SystemExtractionParametersFactory extractionParametersFactory,
                                                               ExtractionParametersFilteringService parametersFilteringService,
                                                               NetSuiteParser netSuiteParser,
                                                               @Value("${lob.events.netsuite.to.core.send.batch.size:100}") int sendBatchSize,
                                                               @Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}") boolean isDebugMode
    ) {
        return new NetSuiteExtractionService(
                ingestionRepository,
                netSuiteClient,
                transactionConverter,
                eventPublisher,
                extractionParametersFactory,
                parametersFilteringService,
                netSuiteParser,
                sendBatchSize,
                NETSUITE_CONNECTOR_ID,
                isDebugMode
        );
    }

    @Bean
    public NetSuiteReconcilationService netsuiteReconcilationService(IngestionRepository ingestionRepository,
                                                                     NetSuiteClient netSuiteClient,
                                                                     TransactionConverter transactionConverter,
                                                                     ApplicationEventPublisher eventPublisher,
                                                                     ExtractionParametersFilteringService parametersFilteringService,
                                                                     NetSuiteParser netSuiteParser,
                                                                     AccountingCoreTransactionRepository accountingCoreTransactionRepository,
                                                                     @Value("${lob.events.netsuite.to.core.send.batch.size:100}") int sendBatchSize,
                                                                     @Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}") boolean isDebugMode
    ) {
        return new NetSuiteReconcilationService(
                ingestionRepository,
                netSuiteClient,
                transactionConverter,
                parametersFilteringService,
                netSuiteParser,
                eventPublisher,
                accountingCoreTransactionRepository,
                sendBatchSize,
                NETSUITE_CONNECTOR_ID,
                isDebugMode
        );
    }

    @Bean
    public NetSuiteEventHandler netSuiteEventHandler(NetSuiteExtractionService netSuiteExtractionService,
                                                     NetSuiteReconcilationService netSuiteReconcilationService) {
        return new NetSuiteEventHandler(netSuiteExtractionService, netSuiteReconcilationService);
    }

    @Bean
    public CodesMappingService codesMappingService(CodesMappingRepository codesMappingRepository) {
        return new CodesMappingService(codesMappingRepository);
    }

    @Bean
    public TransactionTypeMapper transactionTypeMapper() {
        return new TransactionTypeMapper();
    }

    @Bean
    public PreprocessorService preprocessorService() {
        val fieldProcessors = new HashMap<FieldType, Function<String, Either<Problem, String>>>();
        fieldProcessors.put(COST_CENTER, new CostCenterConvertor());
        fieldProcessors.put(PROJECT, new ProjectConvertor());
        fieldProcessors.put(CHART_OF_ACCOUNT, new AccountNumberConvertor());
        fieldProcessors.put(VAT, new VatConvertor());
        fieldProcessors.put(ACCOUNT_CREDIT_NAME, new AccountCreditConvertor());

        return new PreprocessorService(fieldProcessors);
    }

}

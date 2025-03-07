package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.val;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.cardanofoundation.lob.app.accounting_reporting_core.service.assistance.AccountingPeriodCalculator;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.AccountNumberConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.CostCenterConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.ProjectConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.VatConvertor;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.CodesMappingRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.event_handle.NetSuiteEventHandler;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.zalando.problem.Problem;

import java.time.Duration;
import java.util.HashMap;
import java.util.function.Function;

import static org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType.*;

@Configuration
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.app.netsuite_altavia_adapter.config",
        "org.cardanofoundation.lob.app.netsuite_altavia_adapter.service"
})
public class CFConfig {

    private final static String NETSUITE_CONNECTOR_ID = "fEU237r9rqAPEGEFY1yr";

    @Bean
    public RestClient restClient() {
        // Configure HTTP Client with timeouts

        CloseableHttpClient httpClient = HttpClients.custom()
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(Duration.ofSeconds(10)); // Read timeout

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeaders(headers -> {
                    headers.add("Accept", "application/json");
                    headers.add("Content-Type", "application/json");
                })
                .build();
    }


    @Bean
    public NetSuiteClient netSuiteClient(ObjectMapper objectMapper,
                                         RestClient restClient,
                                         @Value("${lob.netsuite.client.url}") String url,
                                         @Value("${lob.netsuite.client.token-url}") String tokenUrl,
                                         @Value("${lob.netsuite.client.private-key-file-path}") String privateKeyFilePath,
                                         @Value("${lob.netsuite.client.client-id}") String clientId,
                                         @Value("${lob.netsuite.client.certificate-id}") String certificateId
    ) {
        return new NetSuiteClient(objectMapper, restClient, url, tokenUrl, privateKeyFilePath, certificateId, clientId);
    }

    @Bean
    public NetSuiteParser netSuiteParser(ObjectMapper objectMapper) {
        return new NetSuiteParser(objectMapper);
    }

    @Bean
    public ExtractionParametersFilteringService extractionParametersFilteringService() {
        return new ExtractionParametersFilteringService();
    }

    @Bean
    public SystemExtractionParametersFactory systemExtractionParametersFactory(OrganisationPublicApiIF organisationPublicApiIF,
                                                                               AccountingPeriodCalculator accountPeriodService)
     {
        return new SystemExtractionParametersFactory(organisationPublicApiIF, accountPeriodService);
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
                sendBatchSize,
                NETSUITE_CONNECTOR_ID,
                isDebugMode
        );
    }

    @Bean
    @ConditionalOnProperty(value = "lob.netsuite.enabled", havingValue = "true", matchIfMissing = true)
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

        return new PreprocessorService(fieldProcessors);
    }

}

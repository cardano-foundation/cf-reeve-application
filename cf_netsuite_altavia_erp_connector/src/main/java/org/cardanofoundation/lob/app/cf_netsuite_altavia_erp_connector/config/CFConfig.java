package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.val;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.zalando.problem.Problem;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 seconds connection timeout
                .responseTimeout(Duration.ofSeconds(10)) // Response timeout
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS)) // Read timeout
                                .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)) // Write timeout
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB Buffer Size
                        .build())
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json");
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public NetSuiteClient netSuiteClient(ObjectMapper objectMapper,
                                         WebClient webClient,
                                         @Value("${lob.netsuite.client.url}") String url,
                                         @Value("${lob.netsuite.client.tokenUrl}") String tokenUrl,
                                         @Value("${lob.netsuite.client.privateKeyFilePath}") String privateKeyFilePath
    ) {
        return new NetSuiteClient(objectMapper, webClient, url, tokenUrl, privateKeyFilePath);
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

package org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import jakarta.validation.Validator;
import lombok.val;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.AccountNumberConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.CostCenterConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.ProjectConvertor;
import org.cardanofoundation.lob.app.cf_netsuite_altavia_erp_connector.convertors.VatConvertor;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuite10Api;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.client.NetSuiteClient;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FinancialPeriodSource;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.CodesMappingRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.repository.IngestionRepository;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.*;
import org.cardanofoundation.lob.app.organisation.OrganisationPublicApiIF;
import org.scribe.builder.ServiceBuilder;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.Problem;

import java.time.Clock;
import java.util.HashMap;
import java.util.function.Function;

import static org.scribe.model.SignatureType.Header;

@Configuration
@ComponentScan(basePackages = {
        "org.cardanofoundation.lob.app.netsuite_altavia_adapter.config",
        "org.cardanofoundation.lob.app.netsuite_altavia_adapter.service"
})
public class CFConfig {

    private final static String NETSUITE_CONNECTOR_ID = "fEU237r9rqAPEGEFY1yr";

    @Bean
    public OAuthService netsuiteOAuthService(
            @Value("${lob.netsuite.client.consumer_key}") String consumerKey,
            @Value("${lob.netsuite.client.consumer_secret}") String consumerSecret
    ) {
        return new ServiceBuilder()
                .apiKey(consumerKey)
                .apiSecret(consumerSecret)
                .signatureType(Header)
                .provider(NetSuite10Api.class)
                .build();
    }

    @Bean
    public NetSuiteClient netSuiteClient(ObjectMapper objectMapper,
                                         OAuthService oAuthService,
                                         @Value("${lob.netsuite.client.url}") String url,
                                         @Value("${lob.netsuite.client.realm}") String realm,
                                         @Value("${lob.netsuite.client.token}") String token,
                                         @Value("${lob.netsuite.client.token_secret}") String tokenSecret
    ) {
        return new NetSuiteClient(oAuthService, objectMapper, url, realm, token, tokenSecret);
    }

    @Bean
    public NetSuiteParser netSuiteParser(ObjectMapper objectMapper) {
        return new NetSuiteParser(objectMapper);
    }

    @Bean
    public ViolationsSenderService violationsSenderService(ApplicationEventPublisher applicationEventPublisher) {
        return new ViolationsSenderService(applicationEventPublisher);
    }

    @Bean
    public ExtractionParametersFilteringService extractionParametersFilteringService(Clock clock) {
        return new ExtractionParametersFilteringService(clock);
    }

    @Bean
    public SystemExtractionParametersFactory systemExtractionParametersFactory(Clock clock,
                                                                               OrganisationPublicApiIF organisationPublicApiIF
    ) {
        return new SystemExtractionParametersFactory(clock, organisationPublicApiIF);
    }

    @Bean("netsuite_adapter.TransactionConverter")
    public TransactionConverter transactionConverter(Validator validator,
                                                     CodesMappingService codesMappingService,
                                                     PreprocessorService preprocessorService,
                                                     TransactionTypeMapper transactionTypeMapper,
                                                     @Value("${lob.events.netsuite.financial.period.source:IMPLICIT}") FinancialPeriodSource financialPeriodSource) {
        return new TransactionConverter(validator,
                codesMappingService,
                preprocessorService,
                transactionTypeMapper,
                NETSUITE_CONNECTOR_ID,
                financialPeriodSource
        );
    }

    @Bean
    public NetSuiteService netSuiteService(IngestionRepository ingestionRepository,
                                           NetSuiteClient netSuiteClient,
                                           ViolationsSenderService violationsSenderService,
                                           TransactionConverter transactionConverter,
                                           ApplicationEventPublisher eventPublisher,
                                           SystemExtractionParametersFactory extractionParametersFactory,
                                           ExtractionParametersFilteringService parametersFilteringService,
                                           NetSuiteParser netSuiteParser,
                                           @Value("${lob.events.netsuite.to.core.send.batch.size:100}") int sendBatchSize,
                                           @Value("${lob.events.netsuite.to.core.netsuite.instance.debug.mode:true}") boolean isDebugMode
    ) {
        return new NetSuiteService(
                ingestionRepository,
                netSuiteClient,
                violationsSenderService,
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
    public NetSuiteEventHandler netSuiteEventHandler(NetSuiteService netSuiteService) {
        return new NetSuiteEventHandler(netSuiteService);
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
        fieldProcessors.put(FieldType.COST_CENTER, new CostCenterConvertor());
        fieldProcessors.put(FieldType.PROJECT, new ProjectConvertor());
        fieldProcessors.put(FieldType.CHART_OF_ACCOUNT, new AccountNumberConvertor());
        fieldProcessors.put(FieldType.VAT, new VatConvertor());

        return new PreprocessorService(fieldProcessors);
    }

}

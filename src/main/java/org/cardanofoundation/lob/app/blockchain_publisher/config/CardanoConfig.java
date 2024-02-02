package org.cardanofoundation.lob.app.blockchain_publisher.config;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.backend.api.BackendService;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import lombok.val;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BackendServiceBlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.BlockchainDataChainTipService;
import org.cardanofoundation.lob.app.blockchain_publisher.service.TransactionSubmissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardanoConfig {

    @Bean
    public Account account() {
        val mnemonic = "ocean sad mixture disease faith once celery mind clay hidden brush brown you sponsor dawn good claim gloom market world online twist laptop thrive";

        return new Account(Networks.preprod(), mnemonic);
    }

    @Bean
    public BackendService backendService() {
        return new BFBackendService("", "");
    }

    @Bean
    public BlockchainDataChainTipService blockchainDataChainTipService(BackendService backendService) {
        return new BackendServiceBlockchainDataChainTipService(backendService());
    }

    @Bean
    public TransactionSubmissionService transactionSubmissionService() {
        return TransactionUtil::getTxHash;
    }

}

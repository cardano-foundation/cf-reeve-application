package org.cardanofoundation.lob.chaindataeventconsumer.store;

import com.bloxbean.cardano.yaci.store.metadata.domain.TxMetadataLabel;
import com.bloxbean.cardano.yaci.store.metadata.storage.impl.jpa.MetadataMapper;
import com.bloxbean.cardano.yaci.store.metadata.storage.impl.jpa.TxMetadataStorageImpl;
import com.bloxbean.cardano.yaci.store.metadata.storage.impl.jpa.repository.TxMetadataLabelRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LedgerEventRegistrationStore extends TxMetadataStorageImpl {
    public LedgerEventRegistrationStore(TxMetadataLabelRepository metadataLabelRepository, MetadataMapper metadataMapper) {
        super(metadataLabelRepository, metadataMapper);
    }

    @Override
    public List<TxMetadataLabel> saveAll(List<TxMetadataLabel> txMetadataLabelList) {
        final List<TxMetadataLabel> filteredMetadataLabels = txMetadataLabelList.stream()
                .filter(txMetadataLabel -> "512".equals(txMetadataLabel.getLabel()))
                .toList();
        return super.saveAll(filteredMetadataLabels);
    }
}

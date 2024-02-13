package org.cardanofoundation.lob.app.blockchain_publisher.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.common.cbor.CborSerializationUtil;
import com.bloxbean.cardano.client.metadata.Metadata;
import com.bloxbean.cardano.client.metadata.MetadataMap;
import com.bloxbean.cardano.client.metadata.helper.MetadataToJsonNoSchemaConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.networknt.schema.SpecVersion.VersionFlag.V7;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataChecker {

    @Value("classpath:modules/blockchain_publisher/blockchain_transaction_metadata_schema.json")
    private Resource metatdataSchemaResource;

    private final ObjectMapper objectMapper;

    public boolean checkTransactionMetadata(MetadataMap metadata) {
        try {
            val data = metadata.getMap();
            val bytes = CborSerializationUtil.serialize(data);

            val json = MetadataToJsonNoSchemaConverter.cborBytesToJson(bytes);

            val jsonObject = objectMapper.readTree(json);

            val jsonSchemaFactory = JsonSchemaFactory.getInstance(V7);

            val schema = jsonSchemaFactory.getSchema(metatdataSchemaResource.getInputStream());

            val validationResult = schema.validate(jsonObject);

            if (!validationResult.isEmpty()) {
                log.error("Metadata validation failed: {}", validationResult);

                return false;
            }

            return true;
        } catch (CborException | IOException e) {
            log.error("Error serializing metadata to cbor", e);
            return false;
        }
    }

}

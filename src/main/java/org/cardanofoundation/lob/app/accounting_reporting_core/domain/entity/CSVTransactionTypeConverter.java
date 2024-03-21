package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class CSVTransactionTypeConverter implements AttributeConverter<List<TransactionType>, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable List<TransactionType> transactionTypes) {
        if (transactionTypes == null || transactionTypes.isEmpty()) {
            return null;
        }

        return transactionTypes
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    @Nullable
    public List<TransactionType> convertToEntityAttribute(@Nullable String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        return Arrays.stream(dbData.split(","))
                .map(s -> Enum.valueOf(TransactionType.class, s))
                .collect(Collectors.toList());
    }

}

package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
@RequiredArgsConstructor
public class CSVTransactionTypeConverter implements AttributeConverter<List<TransactionType>, String> {

    @Override
    public String convertToDatabaseColumn(List<TransactionType> transactionTypes) {
        return transactionTypes
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public @Nullable List<TransactionType> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(","))
                .map(s -> Enum.valueOf(TransactionType.class, s))
                .collect(Collectors.toList());
    }

}

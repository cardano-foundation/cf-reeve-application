package org.cardanofoundation.lob.app.support.orm;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Converter
public class CsvTrimmedStringsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable List<String> attribute) {
        return attribute == null
                ? null
                : attribute.stream().map(String::trim).collect(joining(","));
    }

    @Override
    public @Nullable List<String> convertToEntityAttribute(@Nullable String dbData) {
        return dbData == null
                ? null
                : Arrays.stream(dbData.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}

//package org.cardanofoundation.lob.app.support.orm;
//
//import jakarta.annotation.Nullable;
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Converter
//@RequiredArgsConstructor
//public class CSVEnumConverter<T extends Enum<T>, E extends Class<T>> implements AttributeConverter<List<T>, String> {
//
//    private final Class<T> enumType;
//
//    @Override
//    public @Nullable String convertToDatabaseColumn(@Nullable List<T> attribute) {
//        return attribute == null
//                ? null
//                : attribute.stream().map(Enum::name).collect(Collectors.joining(","));
//    }
//
//    @Override
//    public @Nullable List<T> convertToEntityAttribute(@Nullable String dbData) {
//        return dbData == null
//                ? null
//                : Arrays.stream(dbData.split(","))
//                .map(s -> Enum.valueOf(E, s))
//                .collect(Collectors.toList());
//    }
//
//}

package org.cardanofoundation.lob.app.support.orm;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;
import java.sql.Date;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

@Converter(autoApply = true)
public class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {

    @Override
    @Nullable public Date convertToDatabaseColumn(@Nullable YearMonth attribute) {
        if (attribute != null) {
            return Date.valueOf(
                    attribute.atDay(1)
            );
        }

        return null;
    }

    @Override
    @Nullable public YearMonth convertToEntityAttribute(@Nullable Date dbData) {
        if (dbData != null) {
            return YearMonth.from(
                    Instant
                            .ofEpochMilli(dbData.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
            );
        }

        return null;
    }

}
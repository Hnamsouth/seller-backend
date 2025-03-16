package com.vtp.vipo.seller.common.dao.entity.base.v2;

import com.vtp.vipo.seller.common.utils.DateUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

@Converter
public class LocalDateTimeToLongAttributeConverter implements AttributeConverter<LocalDateTime, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDateTime attribute) {
        if (ObjectUtils.isEmpty(attribute)) {
            return null;
        }
        // Convert LocalDateTime to epoch seconds in the system's default timezone
        return DateUtils.getTimeInSeconds(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long dbData) {
        if (ObjectUtils.isEmpty(dbData)) {
            return null;
        }
        return DateUtils.convertEpochSecondsToLocalDateTime(dbData);
    }
}

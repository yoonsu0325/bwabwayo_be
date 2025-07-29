package com.bwabwayo.app.domain.product.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DeliveryStatusConverter implements AttributeConverter<DeliveryStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DeliveryStatus status) {
        return status != null ? status.getLevel() : null;
    }

    @Override
    public DeliveryStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? DeliveryStatus.fromLevel(dbData) : null;
    }
}

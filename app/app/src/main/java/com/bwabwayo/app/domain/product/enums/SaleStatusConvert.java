package com.bwabwayo.app.domain.product.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SaleStatusConvert implements AttributeConverter<SaleStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SaleStatus status) {
        return status != null ? status.getLevel() : null;
    }

    @Override
    public SaleStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? com.bwabwayo.app.domain.product.enums.SaleStatus.fromLevel(dbData) : null;
    }
}

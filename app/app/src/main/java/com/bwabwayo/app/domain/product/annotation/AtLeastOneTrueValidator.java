package com.bwabwayo.app.domain.product.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class AtLeastOneTrueValidator implements ConstraintValidator<AtLeastOneTrue, Object> {

    private String[] fieldNames;

    @Override
    public void initialize(AtLeastOneTrue constraintAnnotation) {
        this.fieldNames = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            for (String fieldName : fieldNames) {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(object);
                if (Boolean.TRUE.equals(value)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 필드 접근 예외 등은 무시하고 false 처리
        }

        return false;
    }
}

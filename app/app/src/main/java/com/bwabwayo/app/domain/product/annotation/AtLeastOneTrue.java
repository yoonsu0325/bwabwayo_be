package com.bwabwayo.app.domain.product.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AtLeastOneTrue.List.class)
@Constraint(validatedBy = AtLeastOneTrueValidator.class)
@Documented
public @interface AtLeastOneTrue {
    String message() default "지정된 필드 중 하나는 반드시 true여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * 검증할 필드 이름 배열
     */
    String[] fields();

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        AtLeastOneTrue[] value();
    }
}

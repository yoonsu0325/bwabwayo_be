package com.bwabwayo.app.domain.auth.annotation;


import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Parameter(hidden = true)
@Target(ElementType.PARAMETER) //메서드 파라미테어만 사용 가능
@Retention(RetentionPolicy.RUNTIME) //spring 실행 중에 이 어노테이션을 감지하기 위해 RUNTIME으로 설정
public @interface LoginUser { //어노테이션 이름
    boolean required() default true;
}

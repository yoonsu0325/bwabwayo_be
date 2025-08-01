package com.bwabwayo.app.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(ServletContext servletContext) {
        String serverName = servletContext.getVirtualServerName();  // 서버 도메인 정보
        String baseUrl;

        if (serverName != null && serverName.contains("ssafy.io")) {
            baseUrl = "https://i13e202.p.ssafy.io/be"; // 배포 서버 주소
        } else {
            baseUrl = "http://localhost:8081"; // 로컬 개발 주소
        }

        Info info = new Info()
                .title("봐봐요 API 명세서")
                .version("1.0.0");

        String jwtSchemeName = "JWT TOKEN";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .addServersItem(new Server().url(baseUrl))
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}

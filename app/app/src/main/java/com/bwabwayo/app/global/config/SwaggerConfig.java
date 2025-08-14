package com.bwabwayo.app.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 로컬 서버
        Server localServer = new Server()
                .url("http://localhost:8081")
                .description("로컬 개발 서버");

        // 배포 서버
        Server prodServer = new Server()
                .url("https://i13e202.p.ssafy.io/be")
                .description("배포 서버");

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
                .servers(List.of(localServer, prodServer)) // 🔥 서버 목록 등록
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}

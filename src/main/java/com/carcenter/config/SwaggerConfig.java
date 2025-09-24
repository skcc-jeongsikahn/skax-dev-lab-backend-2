package com.carcenter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 설정
 * Spring Boot 3.x + springdoc-openapi 사용
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                    new Server().url("http://localhost:8080" + contextPath).description("로컬 개발 서버"),
                    new Server().url("https://dev-api.car-center.com" + contextPath).description("개발 서버"),
                    new Server().url("https://api.car-center.com" + contextPath).description("운영 서버")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                    .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Car Center API")
                .description("자동차 정비 예약 시스템 REST API 문서")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Car Center Team")
                        .email("dev@car-center.com")
                        .url("https://car-center.com")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("JWT 토큰을 입력하세요. 'Bearer ' 접두사는 자동으로 추가됩니다.");
    }
}

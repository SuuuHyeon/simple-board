package com.example.board.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // 보안 스킴 타입: HTTP
                .scheme("bearer")               // 스킴: bearer (JWT)
                .bearerFormat("JWT")            // 베어러 포맷: JWT
                .in(SecurityScheme.In.HEADER)   // 토큰 위치: 헤더
                .name("Authorization");         // 헤더 이름: Authorization

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        return new OpenAPI().components(new Components().addSecuritySchemes("BearerAuth", securityScheme))
                .security(Collections.singletonList(securityRequirement))
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("Simple-board API List")
                .description("simple-board Swagger UI")
                .version("1.0.0");
    }
}

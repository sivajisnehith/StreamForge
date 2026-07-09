package com.Opsfusionn.StreamForge.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("StreamForge API")
                        .version("1.0")
                        .description("StreamForge REST API documentation with JWT Bearer authentication."))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer uploadEndpointCustomizer() {
        return openApi -> {
            if (openApi.getPaths() != null && openApi.getPaths().containsKey("/api/files/upload")) {
                var pathItem = openApi.getPaths().get("/api/files/upload");
                if (pathItem.getPost() != null) {
                    var post = pathItem.getPost();
                    RequestBody requestBody = post.getRequestBody();
                    if (requestBody != null && requestBody.getContent() != null) {
                        Content content = requestBody.getContent();
                        if (content.containsKey("application/json")) {
                            MediaType mediaType = content.remove("application/json");
                            
                            // Configure encoding so Swagger UI knows the metadata part is application/json
                            Encoding encoding = new Encoding();
                            encoding.setContentType("application/json");
                            mediaType.addEncoding("metadata", encoding);
                            
                            content.put("multipart/form-data", mediaType);
                        }
                    }
                }
            }
        };
    }
}

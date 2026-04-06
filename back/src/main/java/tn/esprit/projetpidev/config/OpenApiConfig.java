package tn.esprit.projetpidev.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI campwayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campway Platform API")
                        .description("""
                                REST API for the Campway camping platform.
                                
                                **Modules:**
                                - 🔐 Auth — Register & Login
                                - 🛒 Marketplace — Equipment, Orders, Payments, Reviews, Coupons
                                - 🚚 Delivery — Vehicles, Deliveries, Ratings
                                
                                **How to authenticate:**
                                1. Call `POST /auth/login` to get your JWT token
                                2. Click the **Authorize** button (🔒) at the top right
                                3. Enter: `Bearer <your_token_here>`
                                4. All protected endpoints will now include your token automatically
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Campway Dev Team")
                                .email("dev@campway.tn"))
                        .license(new License()
                                .name("MIT License")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token. Example: Bearer eyJhbGci...")));
    }
}


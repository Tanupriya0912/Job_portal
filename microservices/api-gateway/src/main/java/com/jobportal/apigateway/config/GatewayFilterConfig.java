package com.jobportal.apigateway.config;

import com.jobportal.apigateway.filter.JwtValidationFilter;
import com.jobportal.apigateway.security.JwtValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayFilterConfig {

    @Bean
    public JwtValidationFilter jwtValidationFilter(JwtValidator jwtValidator) {
        return new JwtValidationFilter(jwtValidator);
    }
}

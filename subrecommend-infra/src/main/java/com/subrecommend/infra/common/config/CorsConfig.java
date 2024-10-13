package com.subrecommend.infra.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    private static final Logger logger = LogManager.getLogger(CorsConfig.class);

    @Value("${FRONT_HOST:http://localhost:3000}")
    private String allowedOrigin;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 오리진 설정
        config.addAllowedOrigin(allowedOrigin);
        logger.info("Allowed Origin: {}", allowedOrigin);

        // 허용할 헤더 설정
        config.addAllowedHeader("*");
        logger.info("Allowed Headers: {}", config.getAllowedHeaders());

        // 허용할 HTTP 메서드 설정
        config.addAllowedMethod("*");
        logger.info("Allowed Methods: {}", config.getAllowedMethods());

        // 자격 증명 허용 (쿠키 등)
        config.setAllowCredentials(true);
        logger.info("Allow Credentials: {}", config.getAllowCredentials());

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
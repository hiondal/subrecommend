package com.subrecommend.infra.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.subrecommend.infra.out.repo")
@EnableTransactionManagement
public class DatabaseConfig {
}
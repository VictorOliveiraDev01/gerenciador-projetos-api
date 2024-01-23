package com.api.gerenciadorprojetos.Projects.Repositories;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.api.gerenciadorprojetos.Projects.Repositories.jpa")
public class JpaConfig {
}

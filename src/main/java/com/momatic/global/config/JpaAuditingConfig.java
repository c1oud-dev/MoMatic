package com.momatic.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 감사(Auditing) 기능 설정 클래스입니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

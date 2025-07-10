package com.momatic.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Debug implements CommandLineRunner {

    // 🔹 Spring Boot가 모든 ClientRegistration 빈을 Iterable 로 주입
    private final Iterable<ClientRegistration> regs;

    public OAuth2Debug(Iterable<ClientRegistration> regs) {
        this.regs = regs;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== ClientRegistrations detected ===");
        regs.forEach(r ->
                System.out.printf(" → id=%s | clientId=%s%n",
                        r.getRegistrationId(), r.getClientId()));
    }

    @Bean
    public CommandLineRunner printFilters(FilterChainProxy proxy) {
        return args -> {
            System.out.println("=== Spring Security Filters ===");
            proxy.getFilterChains().forEach(chain -> {
                System.out.println("  " + chain.getFilters());
            });
        };
    }
}

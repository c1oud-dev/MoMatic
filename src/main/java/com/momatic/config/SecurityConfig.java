package com.momatic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                 // 멀티파트 POST 테스트 용이
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/**").permitAll()  // ✅ REST 열기
                    .anyRequest().authenticated())
            .oauth2Login(o -> o.defaultSuccessUrl("/loginSuccess", true));

        return http.build();
    }
}

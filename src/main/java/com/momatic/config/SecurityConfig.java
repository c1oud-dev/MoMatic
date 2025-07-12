package com.momatic.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                 // 멀티파트 POST 테스트 용이
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/**").permitAll()  // ✅ REST 열기
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth -> oauth
                    // .loginPage("/login")
                    .successHandler(oAuth2SuccessHandler)   // ← 빈으로 관리되는 핸들러 주입
            )
            .logout(logout -> logout
                    .logoutSuccessUrl("/")
            );

        return http.build();
    }
}

package com.momatic.global.config;

import com.momatic.domain.user.service.CustomOAuth2UserService;
import com.momatic.global.security.CustomLogoutSuccessHandler;
import com.momatic.global.security.MockAuthenticationFilter;
import com.momatic.global.security.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/** Spring Security 전역 설정 클래스입니다. */
@Configuration
public class SecurityConfig {

    /** 보안 필터 체인 정책을 구성합니다. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CustomOAuth2UserService customOAuth2UserService,
                                                   OAuth2LoginSuccessHandler successHandler,
                                                   CustomLogoutSuccessHandler logoutSuccessHandler,
                                                   @Autowired(required = false) MockAuthenticationFilter mockAuthenticationFilter) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/plans", "/payments/webhook", "/css/**", "/js/**", "/error/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth.userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(successHandler))
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler))
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/public/**", "/payments/webhook"));

        if (mockAuthenticationFilter != null) {
            http.addFilterBefore(mockAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}


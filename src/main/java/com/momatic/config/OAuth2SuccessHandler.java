package com.momatic.config;

import com.momatic.domain.Team;
import com.momatic.domain.User;
import com.momatic.repository.TeamRepository;
import com.momatic.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final TeamRepository teamRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) auth;
        OAuth2User principal = token.getPrincipal();
        Map<String, Object> attrs = principal.getAttributes();

        // 구글 vs 슬랙 공통 필드 매핑
        String email = (String) attrs.get("email");
        String name  = (String) attrs.getOrDefault("name", email);
        String teamId = (String) attrs.getOrDefault("team_id", "GLOBAL");
        String teamName = (String) attrs.getOrDefault("team_name", teamId);

        Team team = teamRepo.findById(teamId)
                .orElseGet(() -> teamRepo.save(new Team(teamId, teamName)));

        User user = userRepo.findByEmail(email)
                .orElseGet(() -> userRepo.save(
                        User.builder()
                                .email(email)
                                .name(name)
                                .team(team)
                                .roles("ROLE_USER")
                                .build()
                ));

        // 세션에 직접 주입하면 SecurityContext authentication.name 은 email
        req.getSession().setAttribute("currentUserId", user.getId());

        res.sendRedirect("/app");  // 로그인 후 애플리케이션 진입점
    }
}

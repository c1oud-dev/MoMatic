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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final TeamRepository teamRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException, ServletException {
        if (!(auth instanceof OAuth2AuthenticationToken token)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported authentication type");
            return;
        }
        OAuth2User principal = token.getPrincipal();
        Map<String, Object> attrs = principal.getAttributes();

        String email = (String) attrs.get("email");
        if (email == null || email.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing email attribute from provider");
            return;
        }
        String name  = (String) attrs.getOrDefault("name", email);
        String teamName = (String) attrs.getOrDefault("team_name", "GLOBAL");

        Team team = teamRepo.findByName(teamName)
                .orElseGet(() -> {
                    Team created = new Team(teamName);
                    return teamRepo.save(created);
                });

        Optional<User> existingUser = userRepo.findByEmail(email);
        User user = existingUser.orElseGet(() -> {
            User created = new User(email, name, "ROLE_USER");
            created.setTeam(team);
            return userRepo.save(created);
        });

        if (user.getTeam() == null) {
            user.setTeam(team);
            userRepo.save(user);
        }

        // 세션에 직접 주입하면 SecurityContext authentication.name 은 email
        req.getSession().setAttribute("currentUserId", user.getId());

        res.sendRedirect("/app");  // 로그인 후 애플리케이션 진입점
    }
}

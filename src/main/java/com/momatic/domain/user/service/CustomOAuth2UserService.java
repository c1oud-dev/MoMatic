package com.momatic.domain.user.service;

import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/** Google OAuth2 사용자 정보를 동기화하는 서비스입니다. */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** OAuth2 사용자 요청을 처리하고 로컬 사용자 정보를 갱신합니다. */
    @Override
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = String.valueOf(attributes.get("email"));
        String name = String.valueOf(attributes.get("name"));
        User user = userRepository.findByEmail(email)
                .map(found -> { found.updateProfile(name, "ROLE_USER"); return userRepository.save(found); })
                .orElseGet(() -> userRepository.save(User.create(email, name, "ROLE_USER", "google", email)));
        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "email");
    }
}
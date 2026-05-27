package com.momatic.domain.user.controller;

import com.momatic.domain.user.dto.UserResponse;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 조회 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * 현재 인증된 사용자를 조회합니다.
     *
     * @param principal OAuth2 인증 주체
     * @return 사용자 응답
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrent(@AuthenticationPrincipal final OAuth2User principal) {
        final String email = principal.getAttribute("email");
        final User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return ApiResponse.ok(UserResponse.from(user));
    }
}

package com.momatic.domain.user.controller;

import com.momatic.domain.user.dto.UserResponse;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.domain.user.service.UserService;
import com.momatic.global.api.ApiResponse;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 사용자 조회 및 계정 관리 API 컨트롤러입니다.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 현재 인증된 사용자를 조회합니다.
     *
     * @param principal OAuth2 인증 주체
     * @return 사용자 응답
     */
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrent(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return ApiResponse.ok(UserResponse.from(user));
    }

    /**
     * 사용자 설정 페이지를 조회합니다.
     *
     * @return 사용자 설정 페이지 모델과 뷰
     */
    @GetMapping("/settings")
    public ModelAndView settings() {
        return new ModelAndView("user/settings");
    }

    /**
     * 현재 인증된 사용자의 계정을 삭제합니다.
     *
     * @param principal OAuth2 인증 주체
     * @param session 현재 HTTP 세션
     * @return 빈 성공 응답
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteCurrent(@AuthenticationPrincipal OAuth2User principal,
                                           HttpSession session) {
        String email = principal.getAttribute("email");
        userService.deleteAccount(email, session);
        return ApiResponse.ok(null);
    }
}

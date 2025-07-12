package com.momatic.controller;

import com.momatic.domain.User;
import com.momatic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepo;

    @GetMapping("/me")
    public User getCurrent(@AuthenticationPrincipal OAuth2User principal) {
        return userRepo.findByEmail(principal.getAttribute("email")).orElseThrow();
    }
}

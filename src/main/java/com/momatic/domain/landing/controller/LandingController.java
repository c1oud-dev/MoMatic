package com.momatic.domain.landing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** 랜딩 페이지 요청을 처리하는 컨트롤러입니다. */
@Controller
public class LandingController {

    /**
     * 비인증 사용자도 접근할 수 있는 서비스 랜딩 페이지를 표시합니다.
     *
     * @return 랜딩 페이지 템플릿 경로
     */
    @GetMapping("/")
    public String landing() {
        return "landing/index";
    }
}

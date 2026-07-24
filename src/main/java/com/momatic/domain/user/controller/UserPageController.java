package com.momatic.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** 사용자 화면 요청을 처리하는 컨트롤러입니다. */
@Controller
@RequestMapping("/users")
public class UserPageController {

    /**
     * 사용자 설정 페이지를 조회합니다.
     *
     * @return 사용자 설정 페이지 템플릿
     */
    @GetMapping("/settings")
    public String settings() {
        return "user/settings";
    }
}

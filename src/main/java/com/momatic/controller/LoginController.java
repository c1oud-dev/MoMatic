package com.momatic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    @GetMapping("/loginSuccess")
    @ResponseBody
    public String loginSuccess() {
        return "✅ Google 로그인 성공!";
    }
}

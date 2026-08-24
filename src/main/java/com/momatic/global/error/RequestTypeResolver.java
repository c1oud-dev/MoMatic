package com.momatic.global.error;

import jakarta.servlet.http.HttpServletRequest;

/** HTTP 요청이 JSON 응답을 기대하는지 판별합니다. */
public final class RequestTypeResolver {

    private RequestTypeResolver() {
    }

    /**
     * AJAX 또는 JSON 응답 요청 여부를 확인합니다.
     *
     * @param request HTTP 요청
     * @return AJAX 또는 JSON 응답 요청 여부
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }
}
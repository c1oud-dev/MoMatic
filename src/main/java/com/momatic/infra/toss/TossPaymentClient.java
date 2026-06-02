package com.momatic.infra.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.domain.payment.dto.PaymentConfirmRequest;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 토스페이먼츠 승인 API 호출과 Webhook 인증을 담당하는 클라이언트입니다. */
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Value("${app.external.toss.payments.secret-key}")
    private String secretKey;

    /**
     * 결제 승인 API를 호출합니다.
     *
     * @param request 결제 승인 요청
     * @return 토스페이먼츠 승인 응답
     */
    public TossPaymentResponse confirm(PaymentConfirmRequest request) {
        Request tossRequest = new Request.Builder()
                .url(CONFIRM_URL)
                .header("Authorization", createAuthorizationHeader())
                .post(RequestBody.create(createRequestBody(request), JSON_MEDIA_TYPE))
                .build();

        try (Response response = okHttpClient.newCall(tossRequest).execute()) {
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? "" : responseBody.string();
            if (!response.isSuccessful()) {
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }
            return objectMapper.readValue(body, TossPaymentResponse.class);
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    /**
     * Webhook Authorization 헤더가 등록된 시크릿 키와 일치하는지 검증합니다.
     *
     * @param authorizationHeader Webhook Authorization 헤더
     * @return 헤더 일치 여부
     */
    public boolean isValidWebhookAuthorization(String authorizationHeader) {
        if (authorizationHeader == null) {
            return false;
        }
        return MessageDigest.isEqual(
                createAuthorizationHeader().getBytes(StandardCharsets.UTF_8),
                authorizationHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Basic 인증 헤더를 생성합니다.
     *
     * @return Basic 인증 헤더
     */
    private String createAuthorizationHeader() {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * 승인 API JSON 요청 본문을 생성합니다.
     *
     * @param request 결제 승인 요청
     * @return JSON 요청 본문
     */
    private String createRequestBody(PaymentConfirmRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "orderId", request.orderId(),
                    "paymentKey", request.paymentKey(),
                    "amount", request.amount()
            ));
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }
}


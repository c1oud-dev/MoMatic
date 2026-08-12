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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 토스페이먼츠 승인 API 호출과 Webhook 인증을 담당하는 클라이언트입니다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String ORDER_URL = "https://api.tosspayments.com/v1/payments/orders/";
    private static final long CONNECT_TIMEOUT_SECONDS = 5L;
    private static final long READ_TIMEOUT_SECONDS = 15L;
    private static final long WRITE_TIMEOUT_SECONDS = 5L;

    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    @Value("${app.external.toss.payments.secret-key}")
    private String secretKey;

    @Value("${app.external.toss.payments.webhook-secret:${app.external.toss.payments.secret-key}}")
    private String webhookSecret;


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
                .header("Idempotency-Key", createIdempotencyKey(request))
                .post(RequestBody.create(createRequestBody(request), JSON_MEDIA_TYPE))
                .build();

        try (Response response = okHttpClient.newCall(tossRequest).execute()) {
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? "" : responseBody.string();
            if (!response.isSuccessful()) {
                log.error(
                        "토스페이먼츠 승인 실패: orderId={}, status={}, body={}",
                        request.orderId(),
                        response.code(),
                        body
                );
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }
            return objectMapper.readValue(body, TossPaymentResponse.class);
        } catch (IOException exception) {
            throw new TossPaymentNetworkException(exception);
        }
    }

    /**
     * 주문 ID로 토스페이먼츠 결제 상태를 조회합니다.
     *
     * @param orderId 주문 ID
     * @return 조회된 결제 또는 존재하지 않는 경우 빈 값
     */
    public Optional<TossPaymentResponse> findByOrderId(String orderId) {
        Request tossRequest = new Request.Builder()
                .url(ORDER_URL + orderId)
                .header("Authorization", createAuthorizationHeader())
                .get()
                .build();

        try (Response response = okHttpClient.newCall(tossRequest).execute()) {
            if (response.code() == 404) {
                return Optional.empty();
            }
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? "" : responseBody.string();
            if (!response.isSuccessful()) {
                log.error(
                        "토스페이먼츠 결제 조회 실패: orderId={}, status={}, body={}",
                        orderId,
                        response.code(),
                        body
                );
                throw new CustomException(ErrorCode.PAYMENT_CONFIRM_FAILED);
            }
            return Optional.of(objectMapper.readValue(body, TossPaymentResponse.class));
        } catch (IOException exception) {
            throw new TossPaymentNetworkException(exception);
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
                createBasicAuthorizationHeader(webhookSecret).getBytes(StandardCharsets.UTF_8),
                authorizationHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Basic 인증 헤더를 생성합니다.
     *
     * @return Basic 인증 헤더
     */
    private String createAuthorizationHeader() {
        return createBasicAuthorizationHeader(secretKey);
    }

    /**
     * 주문 ID를 기반으로 승인 요청의 멱등성 키를 생성합니다.
     *
     * @param request 결제 승인 요청
     * @return 주문별로 결정적인 멱등성 키
     */
    private String createIdempotencyKey(PaymentConfirmRequest request) {
        return request.orderId();
    }

    /**
     * 지정한 시크릿 기반 Basic 인증 헤더를 생성합니다.
     *
     * @param key Basic 인증에 사용할 시크릿
     * @return Basic 인증 헤더
     */
    private String createBasicAuthorizationHeader(String key) {
        String credentials = key + ":";
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


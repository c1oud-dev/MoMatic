package com.momatic.infra.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.IOException;
import java.util.List;
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

/** OpenAI GPT API를 호출하여 회의 요약과 액션 아이템을 생성하는 클라이언트입니다. */
@Component
@RequiredArgsConstructor
public class GptClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Value("${app.external.openai.api-key}")
    private String apiKey;

    @Value("${app.external.openai.model}")
    private String model;

    /**
     * STT 텍스트를 GPT API로 분석하여 회의 요약과 액션 아이템을 반환합니다.
     *
     * @param transcriptText STT 전사 텍스트
     * @return 회의 분석 결과
     */
    public GptSummaryResult summarize(String transcriptText) {
        Request request = new Request.Builder()
                .url(CHAT_COMPLETIONS_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(createRequestBody(transcriptText), JSON_MEDIA_TYPE))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? "" : responseBody.string();
            if (!response.isSuccessful()) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            return parseResult(body);
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Chat Completions API 요청 본문을 생성합니다.
     *
     * @param transcriptText STT 전사 텍스트
     * @return JSON 요청 본문 문자열
     */
    private String createRequestBody(String transcriptText) {
        Map<String, Object> request = Map.of(
                "model", model,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "회의 전사 텍스트를 분석해 JSON만 응답하세요. "
                                        + "형식은 {\"summary\":\"회의 요약 내용\","
                                        + "\"actionItems\":[{\"content\":\"액션아이템 내용\","
                                        + "\"assignee\":\"담당자명\"}]} 입니다. "
                                        + "담당자가 불명확하면 assignee는 빈 문자열로 둡니다."
                        ),
                        Map.of("role", "user", "content", transcriptText)
                )
        );
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * GPT 응답을 회의 분석 결과 DTO로 변환합니다.
     *
     * @param responseBody GPT API 응답 본문
     * @return 회의 분석 결과
     */
    private GptSummaryResult parseResult(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            GptSummaryResult result = objectMapper.readValue(contentNode.asText(), GptSummaryResult.class);
            validateResult(result);
            return result;
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * GPT 분석 결과의 필수 필드를 검증합니다.
     *
     * @param result GPT 분석 결과
     */
    private void validateResult(GptSummaryResult result) {
        if (result.summary() == null || result.summary().isBlank() || result.actionItems() == null) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
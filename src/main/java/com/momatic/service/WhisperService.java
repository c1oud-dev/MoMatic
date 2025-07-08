package com.momatic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * OpenAI의 Whisper API를 사용하여 음성 파일을 텍스트로 변환(STT)하는 기능을 담당하는 서비스
 * STT(Speech To Text) = 음성을 텍스트로 변환하는 기술
 */
@Slf4j
@Service
public class WhisperService {

    private static final String WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public String transcribe(String filePath) {
        File audioFile = new File(filePath);

        if (!audioFile.exists()) {
            throw new RuntimeException("파일이 존재하지 않습니다: " + filePath);
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody fileBody = RequestBody.create(audioFile, MediaType.parse("audio/mpeg"));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .addFormDataPart("model", "whisper-1")  // ★ 모델명 지정
                .build();

        Request request = new Request.Builder()
                .url(WHISPER_API_URL)
                .addHeader("Authorization", "Bearer " + openaiApiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no response body";
                log.error("Whisper API 호출 실패: {}", errorBody);
                throw new RuntimeException("Whisper API 호출 실패: " + errorBody);
            }

            // ★ 응답에서 텍스트 추출
            String responseBody = response.body().string();
            log.info("Whisper 응답: {}", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.get("text");

            if (textNode == null || textNode.asText().isBlank()) {
                throw new RuntimeException("Whisper 응답에 text가 없습니다");
            }

            return textNode.asText();

        } catch (IOException e) {
            throw new RuntimeException("Whisper 요청 중 오류 발생", e);
        }
    }

}

package com.momatic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class LLMService {

    @Value("${openai.api.key")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String summarizeAndExtractTodos(String transcript) {
        String prompt = """
                아래는 회의에서 전사된 텍스트입니다.

                ---
                %s
                ---

                위 내용을 요약하고, 회의에서 나온 '할 일 목록(TODO)'을 정리해줘.
                출력 형식은 JSON으로 다음과 같이 해줘:

                {
                  "summary": "...",
                  "actionItems": [
                    { "task": "...", "assignee": "...", "dueDate": "..." },
                    ...
                  ]
                }
                """.formatted(transcript);

        try {
            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(
                            objectMapper.readTree("""
                        {
                          "model": "gpt-3.5-turbo",
                          "messages": [
                            { "role": "system", "content": "너는 회의 요약 및 액션 아이템을 정리하는 비서야." },
                            { "role": "user", "content": %s }
                          ]
                        }
                        """.formatted(objectMapper.writeValueAsString(prompt)))
                ),
                MediaType.get("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                log.info("LLM 응답: {}", responseBody);

                if (!response.isSuccessful()) {
                    throw new RuntimeException("GPT API 호출 실패: " + responseBody);
                }

                JsonNode root = objectMapper.readTree(responseBody);
                return root.get("choices").get(0).get("message").get("content").asText();
            }

        } catch (IOException e) {
            throw new RuntimeException("GPT API 호출 중 오류 발생", e);
        }
    }
}

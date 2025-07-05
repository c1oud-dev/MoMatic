package com.momatic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class LLMService {

    private final String openaiApiKey;
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public LLMService(Environment env) {       // ← Environment 주입
        this.openaiApiKey = env.getProperty("openai.api.key");
        if (openaiApiKey == null || openaiApiKey.contains("${")) {
            throw new IllegalStateException("❌ OpenAI API Key not loaded!");
        }
    }

    public String summarizeAndExtractTodos(String transcript) {
        String prompt = """
                아래는 회의 전사 텍스트입니다.

                ---
                %s
                ---

                위 내용을 한국어로 간결히 요약하고,
                JSON 형식으로 액션 아이템을 추출해줘:
                {
                  "summary": "...",
                  "actionItems": [
                    { "task": "...", "assignee": "...", "dueDate": "..." }
                  ]
                }
                """.formatted(transcript);

        try {
            String bodyJson = mapper.writeValueAsString(mapper.readTree("""
              {
                "model":"gpt-3.5-turbo",
                "messages":[
                  { "role":"system","content":"너는 회의 비서야" },
                  { "role":"user","content":%s }
                ]
              }
              """.formatted(mapper.writeValueAsString(prompt))));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + openaiApiKey)   // ✅ 확실히 주입
                    .post(RequestBody.create(bodyJson, MediaType.get("application/json")))
                    .build();

            try (Response res = client.newCall(request).execute()) {
                String resBody = res.body().string();
                if (!res.isSuccessful())
                    throw new RuntimeException("GPT 호출 실패: " + resBody);

                JsonNode root = mapper.readTree(resBody);
                return root.get("choices").get(0).get("message").get("content").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("GPT 요청 중 오류", e);
        }
    }
}

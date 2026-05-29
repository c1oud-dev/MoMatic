package com.momatic.infra.whisper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** OpenAI Whisper STT API를 호출하는 클라이언트입니다. */
@Component
@RequiredArgsConstructor
public class WhisperClient {

    private static final MediaType AUDIO_MEDIA_TYPE = MediaType.parse("application/octet-stream");

    private final ObjectMapper objectMapper;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Value("${app.external.openai.api-key}")
    private String apiKey;

    @Value("${app.external.whisper.api-url}")
    private String apiUrl;

    /**
     * 음성 파일을 Whisper API로 전송하고 전사 텍스트를 반환합니다.
     *
     * @param audioFile 전사할 음성 파일
     * @return Whisper가 반환한 전사 텍스트
     */
    public String transcribe(File audioFile) {
        RequestBody fileBody = RequestBody.create(audioFile, AUDIO_MEDIA_TYPE);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("file", audioFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? "" : responseBody.string();
            if (!response.isSuccessful()) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            return parseText(body);
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Whisper API 응답에서 전사 텍스트를 추출합니다.
     *
     * @param responseBody Whisper API 응답 본문
     * @return 전사 텍스트
     */
    private String parseText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.get("text");
            if (textNode == null || textNode.asText().isBlank()) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            return textNode.asText();
        } catch (IOException exception) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }
}


package com.momatic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackService {

    @Value("${slack.bot.token}")
    private String slackToken;

    @Value("${slack.channel.id}")
    private String channelId;

    private static final String SLACK_POST_URL = "https://slack.com/api/chat.postMessage";
    private final OkHttpClient client = new OkHttpClient();

    public void send(String text) {
        RequestBody body = new FormBody.Builder()
                .add("channel", channelId)
                .add("text", text)
                .build();

        Request req = new Request.Builder()
                .url(SLACK_POST_URL)
                .header("Authorization", "Bearer " + slackToken)
                .post(body)
                .build();

        try (Response res = client.newCall(req).execute()) {
            String resBody = res.body() != null ? res.body().string() : "null";
            log.info("Slack API status={} body={}", res.code(), resBody);

            if (!res.isSuccessful()) {
                log.error("Slack send 실패: {}", res.body().string());
            }
        } catch (Exception e) {
            log.error("Slack 요청 오류", e);
        }
    }
}

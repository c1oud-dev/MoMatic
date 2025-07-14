package com.momatic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momatic.config.SlackProperties;
import com.momatic.domain.Meeting;
import com.momatic.service.MeetingService;
import com.slack.api.Slack;
import com.slack.api.app_backend.SlackSignature;
import com.slack.api.webhook.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/slack")
public class SlackCommandController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SlackSignature.Verifier slackVerifier;
    private final Slack slackClient;
    private final SlackProperties props;
    private final MeetingService meetingService;
    private final ObjectMapper objectMapper;

    public SlackCommandController(
            SlackSignature.Verifier slackVerifier,
            Slack slackClient,
            SlackProperties props,
            MeetingService meetingService,
            ObjectMapper objectMapper
    ) {
        this.slackVerifier   = slackVerifier;
        this.slackClient     = slackClient;
        this.props           = props;
        this.meetingService  = meetingService;
        this.objectMapper    = objectMapper;
    }

    @PostMapping(
            value = "/commands",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<String> onSlashCommand(
            @RequestHeader("X-Slack-Request-Timestamp") String timestamp,
            @RequestHeader("X-Slack-Signature")       String signature,
            @RequestBody                              String body
    ) throws IOException {
        // 1) 서명 검증 (body는 raw form-encoded 바디)
        if (!slackVerifier.isValid(timestamp, signature, body)) {
            log.warn("Invalid Slack signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        // 2) form-data 파싱
        MultiValueMap<String,String> form = UriComponentsBuilder
                .fromUriString("?" + body)
                .build()
                .getQueryParams();

        String command     = form.getFirst("command");
        String text        = form.getFirst("text");
        String responseUrl = form.getFirst("response_url");

        log.info("Received slash command {} with text='{}'", command, text);

        // 3) 비즈니스: 최신 회의 가져오기
        Meeting meeting = meetingService.getLatestMeeting(text);
        // 만약 구현된 메서드명이 다르면 여기를 그 이름으로 바꿔 주세요.

        // 4) Slack Blocks 조립
        List<Map<String,Object>> blocks = new ArrayList<>();
        blocks.add(Map.of(
                "type", "section",
                "text", Map.of("type","mrkdwn", "text","*회의 요약:* " + meeting.getSummary())
        ));
        meeting.getActionItems()
                .forEach(ai -> blocks.add(Map.of(
                        "type","section",
                        "text", Map.of("type","mrkdwn",
                                "text", ":white_small_square: *" + ai.getAssignee() +
                                        "*: " + ai.getTask() +
                                        " (due: " + ai.getDueDate() + ")")
                )));

        // 5) response_url 에 보낼 페이로드
        Map<String,Object> payload = Map.of(
                "response_type", "in_channel",
                "blocks",        blocks
        );
        String payloadJson = objectMapper.writeValueAsString(payload);

        // 6) Slack webhook 호출
        WebhookResponse whRes = slackClient.send(responseUrl, payloadJson);
        log.info("Slack response: code={} body={}", whRes.getCode(), whRes.getBody());

        // 슬래시 커맨드는 빈 200 OK 만 반환해도 됩니다.
        return ResponseEntity.ok("");
    }
}

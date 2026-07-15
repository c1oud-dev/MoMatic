package com.momatic.domain.calendar.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.service.MeetingService;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Google Calendar 외부 API 연동을 처리하는 서비스입니다. */
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String EVENT_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final ActionItemRepository actionItemRepository;
    private final UserRepository userRepository;
    private final MeetingService meetingService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.external.google.calendar-client-id}")
    private String calendarClientId;

    @Value("${app.external.google.calendar-client-secret}")
    private String calendarClientSecret;

    /**
     * 액션 아이템을 Google Calendar 일정으로 등록합니다.
     *
     * @param actionItemId 액션 아이템 ID
     * @param requesterEmail 요청자 이메일
     */
    @Transactional
    public void createEvent(Long actionItemId,
                            String requesterEmail) {
        User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        ActionItem actionItem = actionItemRepository.findById(actionItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        meetingService.validateMeetingEditable(actionItem.getMeeting(), requesterEmail);
        createEvent(user, actionItem);
    }

    /**
     * 액션 아이템 정보를 Google Calendar 일정으로 등록합니다.
     *
     * @param user 캘린더를 보유한 사용자
     * @param actionItem 일정으로 등록할 액션 아이템
     */
    @Transactional
    public void createEvent(User user,
                            ActionItem actionItem) {
        String accessToken = resolveAccessToken(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(createEventPayload(actionItem), headers);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    EVENT_URL,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {
                    }
            );
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("id") == null) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            actionItem.assignCalendarEventId(String.valueOf(responseBody.get("id")));
        } catch (RestClientException ex) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Google Calendar 일정을 삭제합니다.
     *
     * @param user 캘린더를 보유한 사용자
     * @param eventId Google Calendar 일정 ID
     */
    public void deleteEvent(User user,
                            String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }

        String accessToken = resolveAccessToken(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            restTemplate.exchange(
                    EVENT_URL + "/" + eventId,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );
        } catch (RestClientException ex) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 만료 여부에 따라 기존 액세스 토큰 또는 재발급된 액세스 토큰을 반환합니다.
     *
     * @param user 사용자 엔티티
     * @return Google API 액세스 토큰
     */
    private String resolveAccessToken(User user) {
        if (user.getGoogleAccessToken() == null || user.getGoogleAccessToken().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (user.getGoogleTokenExpiresAt() == null || user.getGoogleTokenExpiresAt().isAfter(LocalDateTime.now())) {
            return user.getGoogleAccessToken();
        }
        return refreshAccessToken(user);
    }

    /**
     * 리프레시 토큰으로 Google 액세스 토큰을 재발급하고 사용자 엔티티에 저장합니다.
     *
     * @param user 사용자 엔티티
     * @return 재발급된 액세스 토큰
     */
    private String refreshAccessToken(User user) {
        if (user.getGoogleRefreshToken() == null || user.getGoogleRefreshToken().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", calendarClientId);
        body.add("client_secret", calendarClientSecret);
        body.add("refresh_token", user.getGoogleRefreshToken());
        body.add("grant_type", "refresh_token");

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    TOKEN_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {
                    }
            );
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || responseBody.get("access_token") == null) {
                throw new CustomException(ErrorCode.INTERNAL_ERROR);
            }
            String accessToken = String.valueOf(responseBody.get("access_token"));
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(readExpiresIn(responseBody));
            user.updateGoogleToken(accessToken, user.getGoogleRefreshToken(), expiresAt);
            return accessToken;
        } catch (RestClientException ex) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * Google 토큰 응답에서 액세스 토큰 유효 시간을 읽습니다.
     *
     * @param responseBody 토큰 응답 본문
     * @return 액세스 토큰 유효 시간 초 단위
     */
    private long readExpiresIn(Map<String, Object> responseBody) {
        Object expiresIn = responseBody.get("expires_in");
        if (expiresIn instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(expiresIn));
    }

    /**
     * Google Calendar 일정 생성 요청 본문을 생성합니다.
     *
     * @param actionItem 액션 아이템
     * @return 일정 생성 요청 본문
     */
    private Map<String, Object> createEventPayload(ActionItem actionItem) {
        LocalDate dueDate = actionItem.getDueDate();
        if (dueDate == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("summary", actionItem.getTask());
        payload.put("start", Map.of("date", dueDate.toString()));
        payload.put("end", Map.of("date", dueDate.plusDays(1).toString()));
        return payload;
    }
}


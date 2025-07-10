package com.momatic.service;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {
    private final OAuth2AuthorizedClientService clientService;

    /**
     * @param username  Spring Security principal (예: email)
     * @param summary   일정 제목
     * @param date      하루짜리 전일(all-day) 일정
     */
    public void createEvent(String username, String summary, LocalDate date) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", username);
        if (client == null) {
            log.warn("Google token not found for user {}", username);
            return;
        }

        String token = client.getAccessToken().getTokenValue();

        HttpRequestInitializer init =
                request -> request.getHeaders().setAuthorization("Bearer " + token);

        Calendar cal = new Calendar.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                init)
                .setApplicationName("MoMatic")
                .build();

        Event ev = new Event()
                .setSummary(summary)
                .setStart(new EventDateTime().setDate(new com.google.api.client.util.DateTime(date.toString())))
                .setEnd(new EventDateTime().setDate(new com.google.api.client.util.DateTime(date.plusDays(1).toString())));

        try {
            cal.events().insert("primary", ev).execute();
            log.info("📅 Google Calendar event created: {}", summary);
        } catch (IOException e) {
            log.error("Google Calendar API error", e);
        }
    }
}

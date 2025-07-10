package com.momatic.util;

import org.springframework.stereotype.Component;

import java.time.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Component
public class SimpleKoreanDateParser {

    public Optional<LocalDate> parse(String text) {
        if (text == null) return Optional.empty();
        text = text.trim();

        // ── “다음주 월요일” 패턴 ─────────────────────────
        if (text.startsWith("다음주 ") || text.startsWith("다음 주 ")) {
            String day = text.replace("다음주 ", "").replace("다음 주 ", "");
            return parseThisWeek(day).map(d -> d.plusWeeks(1));
        }

        // ── 이번 주(또는 요일 단독) ──────────────────────
        return parseThisWeek(text);
    }

    /* 내부: 이번 주 요일 파싱 */
    private Optional<LocalDate> parseThisWeek(String dayKo) {
        DayOfWeek dow = switch (dayKo) {
            case "월요일" -> DayOfWeek.MONDAY;
            case "화요일" -> DayOfWeek.TUESDAY;
            case "수요일" -> DayOfWeek.WEDNESDAY;
            case "목요일" -> DayOfWeek.THURSDAY;
            case "금요일" -> DayOfWeek.FRIDAY;
            case "토요일" -> DayOfWeek.SATURDAY;
            case "일요일" -> DayOfWeek.SUNDAY;
            default -> null;
        };
        if (dow == null) return Optional.empty();

        LocalDate today = LocalDate.now();
        return Optional.of(today.with(TemporalAdjusters.nextOrSame(dow)));
    }
}

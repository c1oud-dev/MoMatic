package com.momatic.infra.gpt;

import java.util.List;

/**
 * GPT 회의 분석 결과를 표현하는 DTO입니다.
 *
 * @param summary 회의 요약
 * @param actionItems 액션 아이템 목록
 */
public record GptSummaryResult(
        String summary,
        List<ActionItemResult> actionItems
) {

    /**
     * GPT가 추출한 액션 아이템입니다.
     *
     * @param content 액션 아이템 내용
     * @param assignee 담당자명
     */
    public record ActionItemResult(String content, String assignee) {
    }
}
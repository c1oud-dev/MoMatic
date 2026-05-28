package com.momatic.domain.transcript.dto;

import com.momatic.domain.transcript.entity.Transcript;

/** 전사 응답 DTO입니다. */
public record TranscriptResponse(Long id, String speaker, String content, Double startSec, Double endSec) {

    /** 엔티티를 DTO로 변환합니다. */
    public static TranscriptResponse from(Transcript transcript) {
        return new TranscriptResponse(
                transcript.getId(),
                transcript.getSpeaker(),
                transcript.getContent(),
                transcript.getStartSec(),
                transcript.getEndSec()
        );
    }
}

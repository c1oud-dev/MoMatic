package com.momatic.domain.meeting.repository;

/**
 * 회의 소유자별 회의 수를 나타내는 프로젝션입니다.
 *
 * @param ownerId 회의 소유자 ID
 * @param meetingCount 회의 수
 */
public record MeetingCountByOwner(Long ownerId,
                                  Long meetingCount) {
}

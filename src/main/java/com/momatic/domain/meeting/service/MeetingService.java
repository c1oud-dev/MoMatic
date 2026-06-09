package com.momatic.domain.meeting.service;

import com.momatic.domain.actionItem.entity.ActionItem;
import com.momatic.domain.meeting.entity.Meeting;
import com.momatic.domain.team.entity.TeamMember;
import com.momatic.domain.team.repository.TeamMemberRepository;
import com.momatic.domain.transcript.entity.Transcript;
import com.momatic.domain.actionItem.repository.ActionItemRepository;
import com.momatic.domain.meeting.repository.MeetingRepository;
import com.momatic.domain.transcript.repository.TranscriptRepository;
import com.momatic.domain.user.entity.User;
import com.momatic.domain.user.repository.UserRepository;
import com.momatic.global.error.CustomException;
import com.momatic.global.error.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회의 도메인 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final ActionItemRepository actionItemRepository;
    private final TranscriptRepository transcriptRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.storage-path}")
    private String storagePath;

    /**
     * 인증 사용자가 소유한 회의 목록을 페이징 조회합니다.
     *
     * @param ownerEmail 소유자 이메일
     * @param pageable 페이징 정보
     * @return 소유자 회의 목록
     */
    @Transactional(readOnly = true)
    public Page<Meeting> findOwnedMeetings(String ownerEmail, Pageable pageable) {
        return meetingRepository.findAllByOwnerEmailAndTeamIsNull(ownerEmail, pageable);
    }


    /**
     * 팀 회의 목록을 페이징 조회합니다.
     *
     * @param teamId 팀 ID
     * @param requesterEmail 요청자 이메일
     * @param pageable 페이징 정보
     * @return 팀 회의 목록
     */
    @Transactional(readOnly = true)
    public Page<Meeting> findTeamMeetings(Long teamId,
                                          String requesterEmail,
                                          Pageable pageable) {
        User requester = findUser(requesterEmail);
        validateTeamMembership(teamId, requester.getId());
        return meetingRepository.findAllByTeamId(teamId, pageable);
    }

    /**
     * 회의 단건을 조회합니다.
     *
     * @param id 회의 ID
     * @return 조회된 회의
     */
    @Transactional(readOnly = true)
    public Meeting findMeeting(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
    }

    /**
     * 인증 사용자가 접근 가능한 회의를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     * @return 조회된 회의
     */
    @Transactional(readOnly = true)
    public Meeting findAccessibleMeeting(Long meetingId,
                                         String requesterEmail) {
        Meeting meeting = findMeeting(meetingId);
        validateMeetingReadable(meeting, requesterEmail);
        return meeting;
    }

    /**
     * 인증 사용자 소유의 회의를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 조회된 회의
     */
    @Transactional(readOnly = true)
    public Meeting findOwnedMeeting(Long meetingId, String ownerEmail) {
        return meetingRepository.findByIdAndOwnerEmail(meetingId, ownerEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
    }

    /**
     * 인증 사용자 소유의 회의 상세를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param ownerEmail 소유자 이메일
     * @return 회의 상세
     */
    @Transactional(readOnly = true)
    public MeetingDetail getOwnedMeetingDetail(Long meetingId, String ownerEmail) {
        Meeting meeting = findOwnedMeeting(meetingId, ownerEmail);
        List<ActionItem> actionItems = actionItemRepository.findByMeetingId(meetingId);
        List<Transcript> transcripts = transcriptRepository.findByMeetingId(meetingId);
        return new MeetingDetail(meeting, actionItems, transcripts);
    }

    /**
     * 인증 사용자가 접근 가능한 회의 상세를 조회합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     * @return 회의 상세
     */
    @Transactional(readOnly = true)
    public MeetingDetail getAccessibleMeetingDetail(Long meetingId,
                                                    String requesterEmail) {
        Meeting meeting = findAccessibleMeeting(meetingId, requesterEmail);
        List<ActionItem> actionItems = actionItemRepository.findByMeetingId(meetingId);
        List<Transcript> transcripts = transcriptRepository.findByMeetingId(meetingId);
        return new MeetingDetail(meeting, actionItems, transcripts);
    }

    /**
     * 회의 및 부가 데이터를 저장합니다.
     *
     * @param meeting 저장할 회의 엔티티
     * @param rawTranscript 원문 전사 텍스트
     * @param actionItems 저장할 액션 아이템 목록
     * @return 저장된 회의 엔티티
     */
    @Transactional
    public Meeting saveWithDetails(Meeting meeting,
                                   @Nullable String rawTranscript,
                                   List<ActionItem> actionItems) {
        Meeting savedMeeting = meetingRepository.save(meeting);

        for (ActionItem item : actionItems) {
            item.assignMeeting(savedMeeting);
            actionItemRepository.save(item);
        }
        if (rawTranscript != null && !rawTranscript.isBlank()) {
            Transcript transcript = Transcript.create(
                    "Auto",
                    rawTranscript,
                    0d,
                    (double) rawTranscript.length()
            );
            transcript.assignMeeting(savedMeeting);
            transcriptRepository.save(transcript);
        }

        return savedMeeting;
    }

    /**
     * 인증 사용자가 편집 가능한 회의의 제목을 변경합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     * @param newTitle 변경할 제목
     */
    @Transactional
    public void updateTitle(Long meetingId,
                            String requesterEmail,
                            String newTitle) {
        Meeting meeting = findMeeting(meetingId);
        validateMeetingEditable(meeting, requesterEmail);
        meeting.updateTitle(newTitle);
    }

    /**
     * 인증 사용자가 소유한 회의를 삭제하고 로컬 업로드 파일을 삭제합니다.
     *
     * @param meetingId 회의 ID
     * @param requesterEmail 요청자 이메일
     */
    @Transactional
    public void deleteMeeting(Long meetingId,
                              String requesterEmail) {
        Meeting meeting = findMeeting(meetingId);
        if (!meeting.getOwner().getEmail().equals(requesterEmail)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        String storedFileName = meeting.getStoredFileName();
        meetingRepository.delete(meeting);
        deleteStoredFile(storedFileName);
    }

    /**
     * 회의 조회 가능 여부를 검증합니다.
     *
     * @param meeting 회의
     * @param requesterEmail 요청자 이메일
     */
    private void validateMeetingReadable(Meeting meeting,
                                         String requesterEmail) {
        User requester = findUser(requesterEmail);
        if (!meeting.hasTeam()) {
            if (!meeting.getOwner().getId().equals(requester.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        validateTeamMembership(meeting.getTeam().getId(), requester.getId());
    }

    /**
     * 팀 회의록 편집 가능 여부를 검증합니다.
     *
     * @param meeting 회의
     * @param requesterEmail 요청자 이메일
     */
    public void validateMeetingEditable(Meeting meeting,
                                        String requesterEmail) {
        User requester = findUser(requesterEmail);
        if (!meeting.hasTeam()) {
            if (!meeting.getOwner().getId().equals(requester.getId())) {
                throw new CustomException(ErrorCode.FORBIDDEN);
            }
            return;
        }

        TeamMember member = validateTeamMembership(meeting.getTeam().getId(), requester.getId());
        if (!member.canManageTeam()) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 팀 소속 여부를 검증합니다.
     *
     * @param teamId 팀 ID
     * @param userId 사용자 ID
     * @return 팀 구성원 정보
     */
    private TeamMember validateTeamMembership(Long teamId,
                                              Long userId) {
        return teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));
    }

    /**
     * 사용자 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티
     */
    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 저장소에 남아 있는 회의 업로드 파일을 삭제합니다.
     *
     * @param storedFileName 저장 파일명
     */
    private void deleteStoredFile(String storedFileName) {
        try {
            Path filePath = Paths.get(storagePath).resolve(storedFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR);
        }
    }

    /**
     * 회의 상세 조회 결과입니다.
     */
    public record MeetingDetail(Meeting meeting, List<ActionItem> actionItems, List<Transcript> transcripts) {
    }
}

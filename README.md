# 회의 음성을 전사·요약하고 액션 아이템까지 자동 정리하는 회의록 서비스

> 회의 음성 파일을 업로드하면 전사, 요약, 액션 아이템 추출까지 이어지는 Spring Boot 기반 회의록 관리 서비스입니다.

회의 후 정리 과정에서 반복적으로 발생하는 음성 전사, 핵심 내용 요약, 후속 업무 정리를 자동화하기 위해 개발한 서버 사이드 렌더링 웹 애플리케이션입니다. Google OAuth2 인증, 회의 파일 업로드, 비동기 STT/요약 처리, 액션 아이템 관리, 구독/결제, 팀 협업, Google Calendar 연동을 하나의 Spring Boot 애플리케이션 안에서 도메인별로 분리해 구현했습니다.

![Java](https://img.shields.io/badge/Java-17-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Google OAuth2](https://img.shields.io/badge/Google%20OAuth2-4285F4?style=for-the-badge&logo=google&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-Whisper%20%7C%20GPT--4o--mini-412991?style=for-the-badge&logo=openai&logoColor=white)
![Toss Payments](https://img.shields.io/badge/Toss%20Payments-0064FF?style=for-the-badge&logoColor=white)

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [데모](#데모)
- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [ERD](#erd)
- [DB 스키마 관리](#db-스키마-관리)
- [기술적 의사결정](#기술적-의사결정)
- [트러블슈팅](#트러블슈팅)

## 프로젝트 개요

| 항목 | 내용 |
| --- | --- |
| 개발 기간 | 2025.07 ~ 2025.12 (1차 개발), 2026.05 ~ 진행 중 (전면 재개발) |
| 개발 인원 | 1인 개발 |
| 개발 방식 | Backend, DB 설계, 외부 API 연동, 인프라 전 영역 단독 수행 |

## 데모

<!-- 스크린샷 또는 데모 GIF 추가 예정 -->
![데모](./docs/images/demo.gif)

## 핵심 기능

- **Google OAuth2 인증**: Google 계정으로 로그인하며, 세션 기반으로 접근을 제어합니다. 공개 경로, 인증 필요 경로, 관리자 경로를 분리해 관리합니다.
- **회의 음성 업로드**: mp3, mp4, wav, m4a 형식의 파일을 업로드할 수 있으며, MIME 타입을 검증한 뒤 UUID 기반 파일명으로 저장합니다.
- **회의 비동기 처리**: 업로드가 완료되면 Whisper API로 음성을 전사하고, GPT API로 요약 및 액션 아이템을 추출합니다. 처리 상태는 실시간으로 갱신됩니다.
- **회의 관리**: 회의 목록/상세 조회, 상태 확인, 제목 수정, 삭제, PDF 다운로드를 지원합니다.
- **액션 아이템 관리**: 회의별로 액션 아이템을 생성·수정·삭제하고 상태를 변경할 수 있으며, 전체 액션 아이템을 한눈에 볼 수 있는 목록 페이지를 제공합니다.
- **Google Calendar 연동**: 액션 아이템의 마감일을 기준으로 캘린더 일정을 자동 생성·삭제합니다. 토큰 만료 시 refresh token으로 재발급합니다.
- **구독 및 결제**: Free/Pro/Team 플랜별로 업로드 횟수, 파일 크기 제한, 가격 정책을 운영하며, 토스페이먼츠로 결제 주문 생성부터 승인, Webhook 처리까지 지원합니다.
- **팀 협업**: 팀 생성, 초대 코드 기반 참가, 팀명 수정, 멤버 권한 변경 및 추방/탈퇴 기능을 제공하며, OWNER/ADMIN/MEMBER 역할로 권한을 구분합니다.
- **관리자 콘솔**: 전체 사용자 및 구독 현황을 조회하고, 사용자 플랜을 수동으로 변경할 수 있습니다.
- **사용량 관리**: 사용자별 업로드 횟수와 파일 크기를 기록하고, 매월 사용량을 초기화하는 스케줄러를 운영합니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 3.3.4, Spring Web, Spring Security, Spring Validation, Spring Data JPA |
| View | Thymeleaf SSR |
| Database | MySQL 8(prod), H2(dev/test), Flyway |
| Auth | Google OAuth2 Client, Spring Security Session |
| Async | Spring `@Async`, `ThreadPoolTaskExecutor` |
| External API | OpenAI Whisper API, OpenAI Chat Completions API(`gpt-4o-mini` 기본값), 토스페이먼츠, Google Calendar API, Google OAuth Token API |
| Mail | Spring Boot Starter Mail, 팀 초대 메일 템플릿 |
| PDF | iText 7, html2pdf |
| Test | JUnit 5, Spring Boot Test |
| Build | Gradle, Spring Boot Gradle Plugin |
| CI | GitHub Actions |

## 아키텍처

현재 구조는 단일 Spring Boot 애플리케이션 안에서 도메인 패키지를 분리한 모놀리식 아키텍처입니다. 패키지는 `com.momatic`을 루트로 사용하며, 비즈니스 기능은 `domain`, 공통 설정/예외/응답은 `global`, 외부 API 클라이언트는 `infra`에 배치되어 있습니다.

![아키텍처 다이어그램](./docs/images/architecture.png)

### 회의 처리 흐름

```text
1. 사용자 파일 업로드
2. 파일 확장자/MIME 타입 검증
3. 로컬 스토리지 저장
4. Meeting(PENDING) 및 UsageRecord 저장
5. DB 트랜잭션 커밋 이후 비동기 처리 시작
6. Meeting(PROCESSING) 변경
7. Whisper API 전사
8. GPT API 요약 및 액션 아이템 추출
9. Transcript, ActionItem 저장
10. Meeting(COMPLETED) 변경
11. 예외 발생 시 Meeting(FAILED) 변경
```

## ERD

![ERD](./docs/images/erd.png)

| 엔티티 | 주요 필드/역할 |
| --- | --- |
| `User` | email, name, role, OAuth provider 정보, Google Calendar token |
| `Team` | 팀 이름, 팀 멤버 목록 |
| `TeamMember` | 팀-사용자 매핑, `OWNER`/`ADMIN`/`MEMBER` 역할 |
| `TeamInvite` | 초대 대상 이메일, 초대 코드, 만료 시각, 수락 여부 |
| `Meeting` | 제목, 저장 파일명, 원본 파일명, 처리 상태, 팀, 소유자, 요약 |
| `Transcript` | 회의 전사 발화자, 내용, 시작/종료 초 |
| `ActionItem` | 할 일, 담당자, 마감일, 상태, Google Calendar event id |
| `Subscription` | 사용자 플랜, 구독 상태, 시작/만료 시각 |
| `Payment` | orderId, paymentKey, 결제 금액, 상태, 플랜, 사용자 |
| `UsageRecord` | 사용자별 사용 타입, 사용량, 파일 크기 |

## DB 스키마 관리

DB 스키마는 Flyway로 버전 관리되며, 개인 회의 지원을 위한 nullable 컬럼 변경, 결제 구조 재설계, Google Calendar 연동을 위한 토큰 컬럼 추가 등 총 10개 버전에 걸쳐 점진적으로 발전했습니다.

## 기술적 의사결정

설계 단계에서 의도적으로 선택한 구조입니다.

### 1. 개발 환경에서도 운영 DB와 동일한 방식으로 검증

개발 단계에서는 가벼운 인메모리 데이터베이스(H2)를 사용하면서도, 운영에서 쓰는 MySQL과 동일한 문법 규칙으로 동작하도록 설정했습니다. 덕분에 로컬에서 미리 검증한 데이터베이스 변경 사항을 운영 환경에서도 안심하고 그대로 적용할 수 있습니다.

### 2. 요금제별 정책을 한 곳에서 관리

Free/Pro/Team 요금제마다 다른 업로드 횟수, 파일 크기, 가격 정책을 한 곳에 모아 관리합니다. 업로드 제한, 결제 금액 계산, 요금제 안내 화면이 모두 같은 기준을 참조하기 때문에, 정책이 바뀌어도 한 곳만 수정하면 전체에 일관되게 반영됩니다.

### 3. 외부 서비스 오류를 일관된 방식으로 처리

Whisper, GPT, 토스페이먼츠, Google Calendar 등 외부 서비스 연동 중 문제가 생기면, 종류와 상관없이 동일한 방식의 오류 처리 구조로 변환해서 관리합니다. 어떤 외부 서비스에서 오류가 나든 사용자에게는 일관된 안내가 전달됩니다.

## 트러블슈팅

실제로 문제가 발생해서 원인을 파악하고 구조를 바꿔 해결한 사례입니다.

### 1. OAuth2/OIDC 인증 흐름 불일치로 인한 회원 동기화 누락 해결

#### 문제
Google 로그인 자체는 정상적으로 완료됐지만, 로그인 이후 이어지는 신규 회원 확인·동기화 처리가 정상적으로 동작하지 않았습니다.

#### 원인
Google 인증 요청에 `openid` 스코프가 포함되면서 Spring Security가 이를 OAuth2가 아닌 OIDC 흐름으로 처리하고 있었습니다. 기존에 구현해둔 커스텀 서비스는 OAuth2 기준(`DefaultOAuth2UserService`)으로 작성되어 있어서, OIDC 흐름에서는 이 서비스가 호출되지 않고 Spring Security 기본 `OidcUserService`가 대신 동작하고 있었습니다.

#### 해결
실제 인증 흐름에 맞춰 커스텀 서비스를 OIDC 기준(`OidcUserService`)으로 다시 구현하고 등록했습니다. 이 과정에서 뒤섞여 있던 인증 정보 처리와 신규 회원 확인 절차를 분리해, OIDC 표준을 따르면서도 각 처리 단계의 책임이 명확히 나뉘도록 구조를 개선했습니다.

### 2. afterCommit 후속 작업의 트랜잭션 전파 문제 해결

#### 문제
회의 파일 삭제 요청을 처리했을 때, 삭제 로직은 정상적으로 실행됐지만 실제 DB에는 삭제 이력이 반영되지 않는 문제가 있었습니다.

#### 원인
파일 삭제 이력 저장 로직이 `afterCommit()` 콜백 안에서 실행되고 있었는데, 이 시점은 이미 원래 트랜잭션이 종료된 이후라 별도 전파 옵션 없이 DB 작업을 수행하면 커밋되지 않고 조용히 사라진다는 점을 놓치고 있었습니다.

#### 해결
`afterCommit()` 내부의 저장 로직에 `Propagation.REQUIRES_NEW`를 적용해, 원래 트랜잭션과 분리된 새 트랜잭션에서 확실히 커밋되도록 변경했습니다. 추가로 스케줄러 기반 재확인 로직을 마련해, 삭제 이력이 누락된 경우 후속 작업이 재실행되도록 보완했습니다.

### 3. 비관적 락을 활용한 업로드 횟수 제한 동시성 문제 해결

#### 문제
요금제별 월 업로드 횟수 제한을 두고 있었지만, 같은 사용자가 짧은 시간에 여러 업로드 요청을 동시에 보내면 제한 횟수를 초과해 업로드되는 문제가 있었습니다.

#### 원인
기존 구조는 현재 사용량을 조회한 뒤 제한 초과 여부를 판단하고 반영하는 방식이었는데, 조회와 반영 사이에 시간차가 있다 보니 여러 요청이 동시에 들어오면 서로 같은 시점의 사용량을 읽어가면서 실제로는 제한을 넘었는데도 넘지 않은 것으로 판단되는 경쟁 상태가 발생했습니다.

#### 해결
사용자 사용량을 조회하는 시점에 `PESSIMISTIC_WRITE` 락을 적용해, 동시에 들어온 다른 요청은 처리 중인 요청이 끝날 때까지 대기하도록 변경했습니다. 사용량 조회와 반영을 하나의 트랜잭션 안에서 원자적으로 처리해, 동시 요청이 들어와도 순서대로 하나씩 처리되도록 개선했습니다.
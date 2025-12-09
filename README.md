# MoMatic - AI 기반 회의록 자동화 시스템

## 📋 프로젝트 소개

**MoMatic**은 회의 음성 녹음을 자동으로 텍스트로 변환하고, AI를 활용해 요약 및 액션 아이템을 추출하여 팀 협업 도구(현재 Notion)와 연동하는 통합 회의록 관리 시스템입니다.

### 주요 특징
- 🎙️ **음성 자동 전사**: OpenAI Whisper API를 활용한 STT(Speech-to-Text)
- 🤖 **AI 회의 요약**: GPT를 통한 회의 내용 자동 요약 및 액션 아이템 추출
- 📒 **Notion 정리(계획)**: 회의 결과와 일정 관리를 Notion 워크스페이스로 정리
- 📅 **Google Calendar 연동**: 액션 아이템 자동 일정 등록
- 🔐 **OAuth2 인증**: Google OAuth2 로그인 지원

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Build Tool**: Gradle 8.14.2

### Database
- **Production**: MySQL 
- **Development**: H2 (In-memory)
- **Migration**: Flyway

### Security
- Spring Security
- OAuth2 Client (Google)
- SSL/TLS (HTTPS)

### External APIs & Libraries
- **OpenAI**: Whisper (STT), GPT-3.5 (텍스트 처리)
- **Google**: Calendar API
- **HTTP Client**: OkHttp3
- **JSON Processing**: Jackson

## 📁 프로젝트 구조

```
momatic/
├── src/main/java/com/momatic/
│   ├── config/           # 설정 클래스 (Security, OAuth2)
│   ├── controller/       # REST API 컨트롤러
│   ├── domain/          # JPA 엔티티 (Meeting, User, Team, ActionItem 등)
│   ├── repository/      # 데이터 접근 계층
│   ├── service/         # 비즈니스 로직 (Audio, Whisper, LLM, 등)
│   └── util/            # 유틸리티 클래스
├── src/main/resources/
│   ├── application.properties      # 메인 설정
│   ├── application-dev.properties  # 개발 환경 설정
│   ├── application-prod.properties # 운영 환경 설정
│   └── db/migration/               # Flyway 마이그레이션 스크립트
└── build.gradle
```

## 🚀 시작하기

### 필수 요구사항
- Java 17+
- MySQL (운영 환경)
- 각종 API 키 (OpenAI, Google)

### 환경 변수 설정

`.env` 파일 또는 시스템 환경 변수에 다음 값들을 설정하세요:

```bash
# OpenAI
OPENAI_API_KEY=your-openai-api-key

# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 실행 방법

#### 개발 환경 (H2 Database)
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### 운영 환경 (MySQL)
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### SSL 인증서 설정

프로젝트는 HTTPS를 사용합니다. `src/main/resources/keystore.p12` 파일을 생성하거나 기존 인증서를 사용하세요:

```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650
```

## 📡 API 엔드포인트

### 인증
- `GET /login` - OAuth2 로그인 페이지
- `GET /loginSuccess` - 로그인 성공 콜백

### 오디오 업로드
- `POST /api/audio/upload` - 음성 파일 업로드 및 처리
  - Request: `multipart/form-data` with audio file
  - Response: JSON (요약 및 액션 아이템)

### 사용자 관리
- `GET /api/users/me` - 현재 로그인 사용자 정보

### 팀 관리
- `GET /api/teams` - 팀 목록 조회

## 💾 데이터베이스 스키마

### 주요 테이블

#### Meeting (회의)
- `id`: 회의 ID
- `title`: 회의 제목
- `started_at`: 시작 시간
- `ended_at`: 종료 시간
- `summary`: AI 생성 요약
- `team_id`: 팀 ID (FK)
- `owner_id`: 회의 주최자 (FK)

#### ActionItem (액션 아이템)
- `id`: 액션 아이템 ID
- `task`: 작업 내용
- `assignee`: 담당자
- `due_date`: 마감일
- `status`: 상태 (TODO/IN_PROGRESS/DONE)
- `meeting_id`: 회의 ID (FK)

#### Transcript (전사 기록)
- `id`: 전사 ID
- `speaker`: 발언자
- `content`: 전사 내용
- `start_sec`: 시작 시간
- `end_sec`: 종료 시간
- `meeting_id`: 회의 ID (FK)

#### User (사용자)
- `id`: 사용자 ID
- `email`: 이메일
- `name`: 이름
- `team_id`: 팀 ID (FK)
- `roles`: 권한 (CSV 형태)

#### Team (팀)
- `id`: 팀 ID
- `name`: 팀 이름

## 🔄 워크플로우

1. **음성 업로드**: 사용자가 회의 녹음 파일을 업로드
2. **음성 전사**: Whisper API를 통해 음성을 텍스트로 변환
3. **AI 처리**: GPT가 전사 내용을 분석하여 요약 및 액션 아이템 추출
4. **데이터 저장**: Meeting, Transcript, ActionItem 데이터베이스 저장
5. **통합 알림**:
   - Google Calendar에 액션 아이템 일정 추가
   - 회의 결과/일정은 Notion으로 정리

## 🔧 주요 서비스 컴포넌트

### AudioService
- 음성 파일 업로드 및 저장 관리

### WhisperService
- OpenAI Whisper API를 통한 음성-텍스트 변환

### LLMService
- GPT를 활용한 회의 요약 및 액션 아이템 추출

### GoogleCalendarService
- Google Calendar API를 통한 일정 생성

### MeetingService
- 회의 데이터 통합 관리 및 외부 서비스 연동 조율

## 🔐 보안 설정

### OAuth2 Provider 설정
- **Google**: 프로필, 이메일, 캘린더 권한

### API 접근 권한
- `/api/**` - 인증 없이 접근 가능 (개발 편의)
- 기타 엔드포인트 - 인증 필요

## 📝 환경별 설정

### 개발 환경 (dev)
- H2 인메모리 데이터베이스 사용
- H2 콘솔 활성화 (`/h2-console`)
- DDL 자동 생성 (`update`)

### 운영 환경 (prod)
- MySQL 데이터베이스 사용
- DDL 검증 모드 (`validate`)
- Flyway 마이그레이션 활성화

## 🚧 주의사항

1. **API 키 보안**: 모든 API 키는 환경 변수로 관리하고 절대 코드에 하드코딩하지 마세요
2. **SSL 인증서**: 운영 환경에서는 반드시 유효한 SSL 인증서를 사용하세요
3. **파일 업로드 경로**: `file.upload-dir` 설정을 환경에 맞게 조정하세요
4. **CORS 설정**: 프론트엔드 연동 시 CORS 설정이 필요할 수 있습니다

---

**MoMatic** - Making Meetings Matter 🚀

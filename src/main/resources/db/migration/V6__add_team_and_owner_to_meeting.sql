ALTER TABLE PUBLIC.meeting ADD COLUMN team_id  VARCHAR(255);
ALTER TABLE PUBLIC.meeting ADD COLUMN owner_id BIGINT;

ALTER TABLE PUBLIC.meeting
  ADD CONSTRAINT fk_meeting_team  FOREIGN KEY (team_id)  REFERENCES PUBLIC.team(id);
ALTER TABLE PUBLIC.meeting
  ADD CONSTRAINT fk_meeting_owner FOREIGN KEY (owner_id) REFERENCES PUBLIC.users(id);

-- H2 에서는 "user" 테이블명이 종종 예약어 충돌을 막기 위해 큰따옴표로 감싸야 함
-- MySQL 에서는 user 대신 실제 테이블명이 user면 따옴표 없이 그대로 사용해야 함
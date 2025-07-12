-- 1) 팀 테이블
CREATE TABLE PUBLIC.team (
  id   VARCHAR(255) PRIMARY KEY,
  name VARCHAR(255)
);

CREATE TABLE PUBLIC.users (
  id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  email   VARCHAR(255),
  name    VARCHAR(255),
  team_id VARCHAR(255),
  roles   VARCHAR(255),
  CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES PUBLIC.team(id)
);
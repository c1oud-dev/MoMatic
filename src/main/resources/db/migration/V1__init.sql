-- ============ MEETING =================
CREATE TABLE meeting (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  title         VARCHAR(255),
  started_at    DATETIME,
  ended_at      DATETIME,
  summary       LONGTEXT
);

-- ============ TRANSCRIPT ==============
CREATE TABLE transcript (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  speaker     VARCHAR(100),
  content     LONGTEXT,
  start_sec   DOUBLE,
  end_sec     DOUBLE,
  meeting_id  BIGINT,
  CONSTRAINT fk_transcript_meeting
    FOREIGN KEY (meeting_id) REFERENCES meeting(id)
      ON DELETE CASCADE
);

-- ============ ACTION_ITEM =============
CREATE TABLE action_item (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  task        VARCHAR(255),
  assignee    VARCHAR(100),
  due_date    DATE,
  status      VARCHAR(20),
  meeting_id  BIGINT,
  CONSTRAINT fk_action_meeting
    FOREIGN KEY (meeting_id) REFERENCES meeting(id)
      ON DELETE CASCADE
);
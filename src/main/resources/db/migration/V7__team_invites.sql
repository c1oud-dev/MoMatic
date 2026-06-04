CREATE TABLE team_invites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    inviter_id BIGINT NOT NULL,
    invitee_email VARCHAR(255) NOT NULL,
    code VARCHAR(36) NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    accepted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_team_invites_code UNIQUE (code),
    CONSTRAINT fk_team_invites_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_team_invites_inviter FOREIGN KEY (inviter_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX uk_team_members_team_user
    ON team_members(team_id, user_id);

CREATE INDEX idx_team_invites_team_id
    ON team_invites(team_id);

CREATE INDEX idx_team_invites_inviter_id
    ON team_invites(inviter_id);

CREATE INDEX idx_team_invites_invitee_email
    ON team_invites(invitee_email);
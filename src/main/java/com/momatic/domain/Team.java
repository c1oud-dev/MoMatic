package com.momatic.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "PUBLIC", name = "team")
@Getter
@NoArgsConstructor
public class Team {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;            // ex: Slack team_id
    private String name;          // ex: Slack team name

    public Team(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

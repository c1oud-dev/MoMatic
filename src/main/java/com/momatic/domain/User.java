package com.momatic.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(schema = "PUBLIC", name = "users")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;

    @ManyToOne(optional = false)
    private Team team;

    private String roles;  // CSV e.g. "ROLE_USER,ROLE_ADMIN"

    @Builder
    public User(String email, String name, Team team, String roles) {
        this.email = email;
        this.name = name;
        this.team = team;
        this.roles = roles;
    }

}

package com.petstagram.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "banned_users")
public class BannedEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private UserEntity reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private UserEntity reporterUser;

    @Column(nullable = false)
    private String reason;

    public BannedEntity() {
    }

    public BannedEntity(UserEntity reportedUser, UserEntity reporterUser, String reason) {
        this.reportedUser = reportedUser;
        this.reporterUser = reporterUser;
        this.reason = reason;
    }
}

package com.monad.talep.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ttt_team_messages")
public class TeamMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private AppUser sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public AppUser getSender() { return sender; }
    public void setSender(AppUser v) { this.sender = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
package com.monad.talep.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tt_comments")
public class CommentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id")
    private Request request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Request getRequest() { return request; }
    public void setRequest(Request v) { this.request = v; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser v) { this.user = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

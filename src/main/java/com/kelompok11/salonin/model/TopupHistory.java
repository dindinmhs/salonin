package com.kelompok11.salonin.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topup_histories")
public class TopupHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false, name = "midtrans_id")
    private String midtransId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum Status {
        PENDING, SUKSES, GAGAL
    }

    public TopupHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMidtransId() {
        return midtransId;
    }

    public void setMidtransId(String midtransId) {
        this.midtransId = midtransId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
}

package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.backendspring.Enun.PreferenceType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class UserAIPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferenceType type;

    @Column(nullable = false, length = 500)
    private String value;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}


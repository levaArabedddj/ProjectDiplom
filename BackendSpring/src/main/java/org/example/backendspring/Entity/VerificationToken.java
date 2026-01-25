package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = Users.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private Users user;

    private LocalDateTime expiryDate;

    // Тип токена: PASSWORD или SECRET_PHRASE
    private String type;

    public VerificationToken(Users user, String type) {
        this.user = user;
        this.type = type;
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // Ссылка живет 15 минут
        this.token = UUID.randomUUID().toString();
    }
}

package org.example.tgservice.BD;


import jakarta.persistence.*;
import lombok.*;
import org.example.tgservice.Enum.Gender;
import org.example.tgservice.Enum.UserRole;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "Users_turism")
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long user_id;
    @Column(unique = true)
    private String gmail;
    private String password;
    @Column(unique = true)
    private String userName;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String name;
    private String surname;

    @Column(unique = true,nullable = true)
    private Long telegramChatId;

    private String phone;

    private String securityWord;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

}



package org.example.tgservice.BD;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId; // Telegram ID администратора
    private String name;
    private String email;


}

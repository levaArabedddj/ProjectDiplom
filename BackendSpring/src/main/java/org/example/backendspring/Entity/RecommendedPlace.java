package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "recommended_places")
@Data
public class RecommendedPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private boolean liked = false;
    private boolean disliked = false;


    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;
}



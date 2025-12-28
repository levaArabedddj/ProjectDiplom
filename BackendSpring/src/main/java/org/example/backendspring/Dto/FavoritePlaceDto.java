package org.example.backendspring.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
@AllArgsConstructor
public class FavoritePlaceDto {
    private Long id;
    private String name;
    private String country;

}


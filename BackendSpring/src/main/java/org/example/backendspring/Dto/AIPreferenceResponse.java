package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backendspring.Enun.PreferenceType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPreferenceResponse {
    private Long id;
    private PreferenceType type;
    private String value;
    private boolean active;
}

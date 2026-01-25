package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityStatusDto {
    private boolean hasPassword;
    private boolean hasSecretPhrase;
}

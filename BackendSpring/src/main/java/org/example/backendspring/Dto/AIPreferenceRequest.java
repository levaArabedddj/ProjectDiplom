package org.example.backendspring.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;
import org.example.backendspring.Enun.PreferenceType;

@Data
public class AIPreferenceRequest {

    @NotNull
    private PreferenceType type;

    @NotBlank
    private String value;
}

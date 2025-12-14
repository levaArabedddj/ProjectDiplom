package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backendspring.Enun.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String gmail;
    private String userName;
    private Gender gender;
    private String name;
    private String surname;
    private String phone;
}

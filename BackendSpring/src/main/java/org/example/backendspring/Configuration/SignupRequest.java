package org.example.backendspring.Configuration;
import lombok.Data;
import org.example.backendspring.Enun.Gender;
import org.example.backendspring.Enun.UserRole;

@Data
public class SignupRequest {
    private String userName;
    private String name;
    private String surName;
    private String gmail;
    private String password;
    private Gender gender;
    private String phone;
    private String securityWord;
}

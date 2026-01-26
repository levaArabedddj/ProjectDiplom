package org.example.backendspring.Configuration;

import lombok.Data;

@Data
public class SigninRequest {
    private String userName;
    private String password;
    private Integer code;
}

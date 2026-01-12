package org.example.backendspring;


import org.example.backendspring.Configuration.JwtCore;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Enun.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class JwtCoreTest {

    @Autowired
    JwtCore jwtCore;

    @Test
    void generateAndValidateToken() {

        MyUserDetails userDetails = new MyUserDetails();
        userDetails.setUser_id(1L);
        userDetails.setGmail("user@test.com");
        userDetails.setRole(UserRole.user);

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        String token = jwtCore.generateToken(auth);
        // жду валидній токен
        assertThat(jwtCore.isValidToken(token)).isTrue();
    }
}


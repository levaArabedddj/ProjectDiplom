package org.example.backendspring.Controller;


import jakarta.transaction.Transactional;
import org.example.backendspring.Configuration.JwtCore;
import org.example.backendspring.Configuration.SigninRequest;
import org.example.backendspring.Configuration.SignupRequest;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.UserRole;
import org.example.backendspring.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class SecurityController {


    private UsersRepo usersRepo;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtCore jwtCore;



    @Autowired
    public void setUsersRepo(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
    @Autowired
    public void setJwtCore(JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }


    @PostMapping("/signin")
    ResponseEntity<?> signup(@RequestBody SigninRequest signinRequest) {
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signinRequest.getUserName(), signinRequest.getPassword())
            );

        } catch (BadCredentialsException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtCore.generateToken(authentication);
        return ResponseEntity.ok(jwt);
    }



    @Transactional
    @PostMapping("/signup-Login")
    public ResponseEntity<?> signInAuth(@RequestBody SignupRequest signupRequest){


        if (usersRepo.existsUsersByUserName(signupRequest.getUserName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different name");
        }
        if (usersRepo.existsUsersByGmail(signupRequest.getGmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose different email");
        }


        // Створення користувача
        Users user = new Users();
        user.setUserName(signupRequest.getUserName());
        user.setGmail(signupRequest.getGmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setSecurityWord(passwordEncoder.encode(signupRequest.getSecurityWord()));
        user.setRole(UserRole.user);
        user.setGender(signupRequest.getGender());
        user.setPhone(signupRequest.getPhone());
        user.setName(signupRequest.getName());
        user.setSurname(signupRequest.getSurName());
        usersRepo.save(user);


        try {


            System.out.println(user);
           // emailService.sendRegistrationEmail(user.getGmail(), user.getUserName());

            // Автоматична авторизація
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signupRequest.getUserName(), signupRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }

    }



    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            // возвращаем атрибуты, которые прислал Google (sub, email, name и т.п.)
            return ResponseEntity.ok(oauth2User.getAttributes());
        }
        // fallback — просто имя из токена
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }



}

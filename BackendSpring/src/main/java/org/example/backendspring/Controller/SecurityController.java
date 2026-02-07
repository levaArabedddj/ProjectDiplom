package org.example.backendspring.Controller;


import jakarta.transaction.Transactional;
import org.example.backendspring.Configuration.JwtCore;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Configuration.SigninRequest;
import org.example.backendspring.Configuration.SignupRequest;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.UserRole;
import org.example.backendspring.Repository.UsersRepo;
import org.example.backendspring.Repository.VerificationTokenRepo;
import org.example.backendspring.Service.MailService;
import org.example.backendspring.Service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class SecurityController {


    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final static Logger log = LoggerFactory.getLogger(SecurityController.class);
    private final VerificationTokenRepo verificationTokenRepo;
    private final MailService mailSender;
    private final UsersService usersService;

    @Autowired
    public SecurityController(UsersRepo usersRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtCore jwtCore, VerificationTokenRepo verificationTokenRepo, MailService mailSender, UsersService usersService) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtCore = jwtCore;
        this.verificationTokenRepo = verificationTokenRepo;
        this.mailSender = mailSender;
        this.usersService = usersService;
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

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        Users user = usersRepo.findById(userDetails.getUser_id()).orElseThrow();

        // ПРОВЕРКА: Если у юзера включена 2FA
        if (user.isTwoFactorEnabled()) {
            if (signinRequest.getCode() == null) {
                // Если кода нет в запросе — возвращаем спец. ошибку "Нужен код"
                return ResponseEntity.status(403)
                        .body(Map.of("message", "2FA code required", "requires2fa", true));
            }

            // Если код есть — проверяем его
            boolean valid = usersService.isOtpValid(user.getTwoFactorSecret(), signinRequest.getCode());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
            }
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
            // Автоматична авторизація
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signupRequest.getUserName(), signupRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
        }

    }







}

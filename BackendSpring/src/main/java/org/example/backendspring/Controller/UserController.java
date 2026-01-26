package org.example.backendspring.Controller;

import org.example.backendspring.Configuration.JwtCore;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.SecurityStatusDto;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Entity.VerificationToken;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UsersRepo usersRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtCore jwtCore;
    private final static Logger log = LoggerFactory.getLogger(SecurityController.class);
    private final VerificationTokenRepo verificationTokenRepo;
    private final MailService mailSender;
    private final UsersService usersService;

    @Autowired
    public UserController(UsersRepo usersRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtCore jwtCore, VerificationTokenRepo verificationTokenRepo, MailService mailSender, UsersService usersService) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtCore = jwtCore;
        this.verificationTokenRepo = verificationTokenRepo;
        this.mailSender = mailSender;
        this.usersService = usersService;
    }


    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal MyUserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 1. Достаем свежего юзера из базы
        Users user = usersRepo.findById(currentUser.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> response = new java.util.HashMap<>();

        response.put("id", user.getUser_id());
        response.put("userName", user.getUserName());
        response.put("gmail", user.getGmail());
        response.put("name", user.getName());
        response.put("surname", user.getSurname());
        response.put("phone", user.getPhone());
        response.put("gender", user.getGender());
        response.put("twoFactorEnabled", user.isTwoFactorEnabled());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/security-status")
    public ResponseEntity<?> getSecurityStatus(@AuthenticationPrincipal MyUserDetails currentUser) {

        if (currentUser == null) {
            // Если токена нет или он кривой — возвращаем 401, а не ошибку сервера 500
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        log.info(String.valueOf(currentUser.getUser_id()));
        Users user = usersRepo.findById(currentUser.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
        boolean hasSecretPhrase = user.getSecurityWord() != null && !user.getSecurityWord().isEmpty();

        return ResponseEntity.ok(new SecurityStatusDto(hasPassword, hasSecretPhrase));
    }

    @PostMapping("/request-setup")
    public ResponseEntity<?> requestSetup(@AuthenticationPrincipal MyUserDetails currentUser,
                                          @RequestBody Map<String, String> payload) {

        String type = payload.get("type");
        if (type == null || (!type.equals("PASSWORD") && !type.equals("SECRET"))) {
            return ResponseEntity.badRequest().body("Invalid setup type");
        }

        Users user = usersRepo.findById(currentUser.getUser_id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Создаем токен
        VerificationToken token = new VerificationToken(user, type);
        verificationTokenRepo.save(token);

        // 2. Формируем ссылку (Используем твой реальный домен)
        String link = "https://triplevad.duckdns.org/setup?token=" + token.getToken() + "&type=" + type;

        mailSender.sendSetupEmail(user.getGmail(), user.getName(), link, type);

        return ResponseEntity.ok("Link sent to " + user.getGmail());
    }

    // ШАГ 2: Юзер перешел по ссылке и ввел новый пароль
    @PostMapping("/public/finish-setup")
    public ResponseEntity<?> finishSetup(@RequestBody Map<String, String> payload) {
        String tokenStr = payload.get("token");
        String value = payload.get("value");

        VerificationToken token = verificationTokenRepo.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        Users user = token.getUser();

        if ("PASSWORD".equals(token.getType())) {
            if (value.length() < 6) return ResponseEntity.badRequest().body("Password too short");
            user.setPassword(passwordEncoder.encode(value));
        } else if ("SECRET".equals(token.getType())) {
            user.setSecurityWord(passwordEncoder.encode(value));
        }

        usersRepo.save(user);
        verificationTokenRepo.delete(token); // Удаляем использованный токен

        return ResponseEntity.ok("Updated successfully");
    }


    // Юзер нажал "Включить 2FA". Мы даем ему QR-код.
    @GetMapping("/2fa/setup")
    public ResponseEntity<?> setup2fa(@AuthenticationPrincipal MyUserDetails currentUser) {
        Users user = usersRepo.findById(currentUser.getUser_id()).orElseThrow();

        // Генеруємо новий секрет
        String secret = usersService.generateNewSecret();

        user.setTwoFactorSecret(secret);
        usersRepo.save(user);

        String qrUrl = usersService.generateQrCodeUrl(secret, user.getGmail());

        return ResponseEntity.ok(Map.of(
                "qrUrl", qrUrl,
                "secret", secret
        ));
    }

    // Юзер отсканировал и ввел код подтверждения. Теперь включаем!
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2fa(@AuthenticationPrincipal MyUserDetails currentUser,
                                       @RequestBody Map<String, Integer> payload) {
        Users user = usersRepo.findById(currentUser.getUser_id()).orElseThrow();
        Integer code = payload.get("code");

        if (usersService.isOtpValid(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(true); // <--- ВОТ ТУТ АКТИВИРУЕМ
            usersRepo.save(user);
            return ResponseEntity.ok("2FA successfully enabled");
        } else {
            return ResponseEntity.badRequest().body("Invalid code");
        }
    }
}

package org.example.backendspring.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class UsersService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // 1. Генерируем новый секретный ключ (для настройки)
    public String generateNewSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }


    // 2. Генерируем ссылку для QR-кода
    public String generateQrCodeUrl(String secret, String email) {
        String issuer = "TravelApp";
        String account = (email != null && !email.isEmpty()) ? email : "User";

        try {
            // Кодируем строки, чтобы убрать запрещенные символы (пробелы, @, :)
            String label = URLEncoder.encode(issuer + ":" + account, StandardCharsets.UTF_8);
            String issuerParam = URLEncoder.encode(issuer, StandardCharsets.UTF_8);

            // Правильный формат Google Auth:
            // otpauth://totp/Issuer:Account?secret=SECRET&issuer=Issuer
            return String.format("otpauth://totp/%s?secret=%s&issuer=%s",
                    label, secret, issuerParam);
        } catch (Exception e) {
            // На случай проблем с кодировкой (почти невозможно для UTF-8)
            e.printStackTrace();
            return "";
        }
    }

    // 3. Проверяем код (валиден ли он?)
    public boolean isOtpValid(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}

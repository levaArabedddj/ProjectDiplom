package org.example.backendspring.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

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
        String accountName = (email != null && !email.isEmpty()) ? email : "User";

        return String.format("otpauth://totp/TravelApp:%s?secret=%s&issuer=TravelApp",
                accountName, secret);
    }

    // 3. Проверяем код (валиден ли он?)
    public boolean isOtpValid(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}

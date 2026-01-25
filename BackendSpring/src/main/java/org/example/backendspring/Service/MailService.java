package org.example.backendspring.Service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRecommendationEmail(String to, String greeting, List<String> places) {
        try {
            String subject = "Ваши персональные рекомендации для путешествий";

            // Формируем HTML
            StringBuilder sb = new StringBuilder();
            sb.append("<h2>").append(greeting).append("</h2>");
            sb.append("<p>Мы подобрали для вас несколько идей:</p>");
            sb.append("<ul>");
            for (String place : places) {
                sb.append("<li>").append(place).append("</li>");
            }
            sb.append("</ul>");
            sb.append("<p>Если вас заинтересовали эти города — ");
            sb.append("зайдите на сайт и выберите понравившиеся. ");
            sb.append("После этого вы сможете начать организацию своего отдыха прямо у нас!</p>");

            // Создаем сообщение
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(sb.toString(), true); // true = HTML

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при отправке письма", e);
        }
    }

    public void sendSetupEmail(String to, String name, String link, String type) {
        try {
            String subject;
            String actionName;

            if ("PASSWORD".equals(type)) {
                subject = "Встановлення пароля | Travel App";
                actionName = "встановити новий пароль";
            } else {
                subject = "Секретна фраза | Travel App";
                actionName = "зберегти секретну фразу";
            }

            // Красивый HTML
            StringBuilder sb = new StringBuilder();
            sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
            sb.append("<h2>Вітаємо, ").append(name).append("!</h2>");
            sb.append("<p>Ви (або хтось від вашого імені) зробили запит, щоб <b>").append(actionName).append("</b>.</p>");
            sb.append("<p>Для продовження натисніть на кнопку нижче:</p>");

            sb.append("<a href='").append(link).append("' style='");
            sb.append("background-color: #646cff; color: white; padding: 12px 24px; ");
            sb.append("text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;");
            sb.append("'>Перейти до налаштувань</a>");

            sb.append("<p style='margin-top: 20px; font-size: 12px; color: #777;'>");
            sb.append("Посилання дійсне протягом 15 хвилин.<br>");
            sb.append("Якщо ви не робили цей запит, просто ігноруйте цей лист.");
            sb.append("</p></div>");

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(sb.toString(), true); // true = HTML

            mailSender.send(message);
        } catch (Exception e) {
            // Логируем ошибку, но не роняем приложение, если почта не ушла (или роняем, если критично)
            e.printStackTrace();
            throw new RuntimeException("Не вдалося відправити лист на " + to, e);
        }
    }



}


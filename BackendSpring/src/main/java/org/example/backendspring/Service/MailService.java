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




}


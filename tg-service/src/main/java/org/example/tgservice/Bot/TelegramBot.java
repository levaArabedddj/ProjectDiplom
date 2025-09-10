package org.example.tgservice.Bot;

import lombok.extern.slf4j.Slf4j;
import org.example.tgservice.BD.Feedback;
import org.example.tgservice.BD.Users;
import org.example.tgservice.Repo.FeedbackRepo;
import org.example.tgservice.BD.TelegramUser;
import org.example.tgservice.Repo.UserRepo;
import org.example.tgservice.Repo.UsersRepo;
import org.example.tgservice.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UserRepo userRepository;
    private final UsersRepo usersRepository;
    private final FeedbackRepo feedbackRepo;
    private final OpenAIService openAIService;
    private final GoogleSheetsService googleSheetsService;
    private final TrelloService trelloService;
    private final Map<Long, Integer> loginAttempts = new HashMap<>();
    private final Map<Long, LocalDateTime> blockedUntil = new HashMap<>();
    private final int MAX_ATTEMPTS = 3;
    private final int BLOCK_TIME_MINUTES = 10;
    private final String ADMIN_PASSWROD = System.getenv("ADMIN_PASSWROD");
    private final Set<Long> adminChatIds = new HashSet<>();
    private final Map<Long, Boolean> waitingForPassword = new HashMap<>();
    private final UserService userService;
    // хранение состояний для авторизации
    private final Map<Long, Boolean> waitingForEmail = new HashMap<>();
    private final Map<Long, Boolean> waitingForPhone = new HashMap<>();
    private final Map<Long, Boolean> waitingForName = new HashMap<>();

    private final Map<Long, String> tempEmails = new HashMap<>();
    private final Map<Long, String> tempPhones = new HashMap<>();
    private final Map<Long, String> tempNames = new HashMap<>();
    private final Map<Long,String> waitingForCode = new HashMap<>();
    private final Map<Long, String> tempResetEmails = new HashMap<>();

    // состояние для смены пароля
    private final Map<Long, Boolean> waitingForSecurityWord = new HashMap<>();
    private final Map<Long, Boolean> waitingForNewPassword = new HashMap<>();
    private final Map<Long,Boolean> waitingForEmailReset = new HashMap<>();

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Autowired
    public TelegramBot(BotConfig botConfig, UserRepo userRepository, UsersRepo usersRepository, FeedbackRepo feedbackRepo,
                       OpenAIService openAIService, GoogleSheetsService googleSheetsService,
                       TrelloService trelloService, UserService userService, MailService mailService) {
        this.botConfig = botConfig;
        this.userRepository = userRepository;
        this.usersRepository = usersRepository;
        this.feedbackRepo = feedbackRepo;
        this.openAIService = openAIService;
        this.googleSheetsService = googleSheetsService;
        this.trelloService = trelloService;
        this.userService = userService;
        this.mailService = mailService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();


        // рефакторінг завдяки горячим клавішам в Інтелідж, обережно через клавіши переводим у приватні методи
        if (handleAuth(chatId, text)) return; // авторізація адміна

        if (adminChatIds.contains(chatId)) {
            if (handleAdminCommands(chatId, text)) return; // команди авторізованого адміна
        }

        if (text.equalsIgnoreCase("/login")) {
            sendMessage(chatId, "✉️ Введите ваш email:");
            waitingForEmail.put(chatId, true);
            return;
        }

        if (waitingForEmail.getOrDefault(chatId, false)) {
            tempEmails.put(chatId, text.trim());
            sendMessage(chatId, "📱 Теперь введите ваш телефон:");
            waitingForEmail.remove(chatId);
            waitingForPhone.put(chatId, true);
            return;
        }

        if(waitingForPhone.getOrDefault(chatId,false)){
            tempPhones.put(chatId, text.trim());
            sendMessage(chatId,"Введите ваше имя записанное при регистрации на сайте");
            waitingForPhone.remove(chatId);
            waitingForName.put(chatId, true);
            return;
        }

        if (waitingForName.getOrDefault(chatId, false)) {
            tempNames.put(chatId, text.trim());

            String email = tempEmails.get(chatId);
            String phone = tempPhones.get(chatId);
            String name = tempNames.get(chatId);

            Optional<Users> usersOptional = usersRepository.findByGmailAndPhoneAndName(email, phone, name);

            if (usersOptional.isPresent()) {
                Users users = usersOptional.get();
                if (users.getTelegramChatId() != null) {
                    sendMessage(chatId, "Эта учетная запись уже авторизована, вы не можете дважды авторизоватся в учетной записи");
                } else {
                    userService.linkTelegramAccount(chatId, email, phone, name)
                            .ifPresentOrElse(
                                    user -> sendMessage(chatId, "✅ Авторизация успешна! Привязка к аккаунту: " + user.getUserName()),
                                    () -> sendMessage(chatId, "❌ Неверный email, телефон или имя. Попробуйте снова.")
                            );
                }
            } else {
                sendMessage(chatId, "❌ Неверный email, телефон или имя. Попробуйте снова.");
            }

            // Очистка временных данных в любом случае
            waitingForName.remove(chatId);
            tempEmails.remove(chatId);
            tempPhones.remove(chatId);
            tempNames.remove(chatId);
        }


        // проверяем, авторизован ли пользователь
        Optional<Users> authUser = userService.findByTelegramChatId(chatId);

        if (authUser.isPresent()) {
            handleFeedbackAuthorized(chatId, text, authUser.get());
        }

        if(text.toLowerCase().startsWith("/editemail")){
            String email = text.substring(10).trim();
            userService.updateEmail(chatId,email);
            sendMessage(chatId, "📧 Email обновлён на: " + email);
            return;
        }
        if(text.toLowerCase().startsWith("/editphone")){
            String phone = text.substring(10).trim();
            userService.updatePhone(chatId,phone);
            sendMessage(chatId, "📱 Телефон обновлён на: " + phone);
            return;
        }

        if(text.toLowerCase().startsWith("/editpassword")){
            sendMessage(chatId, "🛡 Введите ваше секретное слово для подтверждения:");
            waitingForSecurityWord.put(chatId, true);
            return;
        }

        // Проверка секретного слова
        if(waitingForSecurityWord.getOrDefault(chatId, false)){
            String securityWordInput = text.trim();
            Optional<Users> userOpt = userService.findByTelegramChatId(chatId);

            if(userOpt.isPresent() && passwordEncoder.matches(securityWordInput, userOpt.get().getSecurityWord())){
                waitingForSecurityWord.remove(chatId);
                waitingForNewPassword.put(chatId, true);
                sendMessage(chatId, "✅ Секретное слово верно. Введите новый пароль:");
            } else {
                sendMessage(chatId, "❌ Секретное слово неверно. Попробуйте снова.");
            }
            return;
        }

        // Ввод нового пароля
        if(waitingForNewPassword.getOrDefault(chatId, false)){
            String newPassword = text.trim();
            userService.updatePasswordByTelegramChatId(chatId, newPassword);
            waitingForNewPassword.remove(chatId);
            sendMessage(chatId, "✅ Пароль успешно изменен!");
            return;
        }

        if(text.toLowerCase().startsWith("/resetpassword")){
            sendMessage(chatId, "Введите вашу почту для сброса пароля");
            waitingForEmailReset.put(chatId,true);
            return;
        }

        if (waitingForEmailReset.getOrDefault(chatId, false)){
            String gmail = text.trim();
            Optional<Users> usersOptional = usersRepository.findByGmail(gmail);

            if(usersOptional.isPresent()){
                Users users = usersOptional.get();
                String code = genereatCode();
                users.setResetToken(code);
                users.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
                usersRepository.save(users);

                mailService.sendResetPasswordMail(users.getGmail(),code);
                sendMessage(chatId,"Код для сброса вашего пароля отправлен на вашу почту, Введите его сюда и отправьте нам");
                waitingForEmailReset.remove(chatId);
                waitingForCode.put(chatId, gmail);
            }else {
                sendMessage(chatId, "❌ Пользователь с такой почтой не найден.");
            }
        }

        if (waitingForCode.containsKey(chatId)) {
            String email = waitingForCode.get(chatId);
            Optional<Users> userOpt = usersRepository.findByGmail(email);

            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                if (user.getResetToken().equals(text.trim()) &&
                        user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {

                    sendMessage(chatId, "✅ Код верный. Введите новый пароль:");
                    waitingForCode.remove(chatId);
                    waitingForNewPassword.put(chatId, true);

                } else {
                    sendMessage(chatId, "❌ Неверный или просроченный код.");
                }
            }
            return;
        }
        if (waitingForNewPassword.getOrDefault(chatId, false)) {
            String newPassword = text.trim();
            String email = tempResetEmails.get(chatId);

            Users user = usersRepository.findByGmail(email).orElseThrow();
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);

            sendMessage(chatId, "✅ Пароль успешно изменён!");

            waitingForNewPassword.remove(chatId);
            tempResetEmails.remove(chatId);
        }

    }

    private String genereatCode() {
        Random rand = new Random();
        int code = 100000+ rand.nextInt(900000);
        return String.valueOf(code);
    }


    private void handleFeedbackAuthorized(Long chatId, String text, Users user) {
        if (text.toLowerCase().startsWith("/feedback")) {
            String feedbackText = text.length() > 9 ? text.substring(9).trim() : "";

            if (feedbackText.isEmpty()) {
                sendMessage(chatId, "Будь ласка, введіть ваш відгук після команди /feedback.");
                return;
            }

            Feedback feedback;
            try {
                feedback = openAIService.analyzeFeedback(chatId, feedbackText);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                sendMessage(chatId, "❌ Помилка при аналізі відгуку. Спробуйте пізніше.");
                log.error("OpenAI API error", e);
                return;
            } catch (ResourceAccessException e) {
                sendMessage(chatId, "❌ Проблема з підключенням до OpenAI. Спробуйте пізніше.");
                log.error("OpenAI connection error", e);
                return;
            } catch (Exception e) {
                sendMessage(chatId, "❌ Невідома помилка при аналізі відгуку.");
                log.error("Unexpected error while analyzing feedback", e);
                return;
            }

            feedback.setBranch(user.getUserName()); // или роль/філія
            feedback.setRole(user.getRole().name());
            feedbackRepo.save(feedback);

            googleSheetsService.appendFeedback(feedback);
            trelloService.createCard(feedback);

            sendMessage(chatId,
                    "Ваш відгук отримано 🙌\n" +
                            "Тональність: " + feedback.getSentiment() + "\n" +
                            "Критичність: " + feedback.getCriticality() + "/5\n" +
                            "Рішення: " + feedback.getSuggestion());
        }
    }


    private boolean handleAuth(Long chatId, String text) {
        if (waitingForPassword.getOrDefault(chatId, false)) {
            waitingForPassword.remove(chatId);

            int attempts = loginAttempts.getOrDefault(chatId, 0);

            if (blockedUntil.containsKey(chatId)) {
                if (blockedUntil.get(chatId).isAfter(LocalDateTime.now())) {
                    sendMessage(chatId, "Слишком много попыток. Попробуйте позже.");
                    return true;
                } else {
                    blockedUntil.remove(chatId);
                    loginAttempts.put(chatId, 0);
                }
            }

            if (text.equals(ADMIN_PASSWROD)) {
                loginAttempts.remove(chatId);
                adminChatIds.add(chatId);
                sendMessage(chatId, "Пароль верный ✅. Вы теперь админ. Используйте команды...");
            } else {
                attempts++;
                loginAttempts.put(chatId, attempts);
                if (attempts >= MAX_ATTEMPTS) {
                    blockedUntil.put(chatId, LocalDateTime.now().plusMinutes(BLOCK_TIME_MINUTES));
                    sendMessage(chatId, "Забагато  спроб. Спробуйте через " + BLOCK_TIME_MINUTES + " минут.");
                } else {
                    sendMessage(chatId, "Невірний пароль ❌. Залишилось спроб: " + (MAX_ATTEMPTS - attempts));
                }
            }
            return true;
        }

        if (text.equalsIgnoreCase("/admin")) {
            waitingForPassword.put(chatId, true);
            sendMessage(chatId, "Введіть пароль администратора:");
            return true;
        }
        return false;
    }

    private void sendMessage(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatFeedbackList(List<Feedback> feedbacks) {
        if (feedbacks.isEmpty()) return "Немає відгуків 😔";
        StringBuilder sb = new StringBuilder();
        for (Feedback f : feedbacks) {
            sb.append("ID: ").append(f.getId()).append("\n")
                    .append("Відгук: ").append(f.getMessage()).append("\n")
                    .append("Тональність: ").append(f.getSentiment()).append("\n")
                    .append("Критичність: ").append(f.getCriticality()).append("\n")
                    .append("Роль/Філія: ").append(f.getRole()).append(", ").append(f.getBranch())
                    .append("\n\n");
        }
        return sb.toString();
    }

    private boolean handleAdminCommands(Long chatId, String text) {
        if (!adminChatIds.contains(chatId)) return false;

        if (text.equalsIgnoreCase("/all")) {
            sendMessage(chatId, formatFeedbackList(feedbackRepo.findAll()));
            return true;
        }
        if (text.toLowerCase().startsWith("/branch ")) {
            String branch = text.substring(8).trim();
            sendMessage(chatId, formatFeedbackList(
                    feedbackRepo.findAll().stream()
                            .filter(f -> f.getBranch().equalsIgnoreCase(branch))
                            .toList()
            ));
            return true;
        }
        if (text.toLowerCase().startsWith("/role ")) {
            String role = text.substring(6).trim();
            sendMessage(chatId, formatFeedbackList(
                    feedbackRepo.findAll().stream()
                            .filter(f -> f.getRole().equalsIgnoreCase(role))
                            .toList()
            ));
            return true;
        }
        if (text.toLowerCase().startsWith("/critical ")) {
            try {
                int lvl = Integer.parseInt(text.substring(10).trim());
                sendMessage(chatId, formatFeedbackList(
                        feedbackRepo.findAll().stream()
                                .filter(f -> f.getCriticality() >= lvl)
                                .toList()
                ));
            } catch (NumberFormatException e) {
                sendMessage(chatId, "Неверный уровень критичности");
            }
            return true;
        }
        return false;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}

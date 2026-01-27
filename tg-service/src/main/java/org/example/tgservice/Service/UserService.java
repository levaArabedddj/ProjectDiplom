package org.example.tgservice.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.example.tgservice.BD.Users;
import org.example.tgservice.Repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private final UsersRepo usersRepo;

    public Optional<Users> findByTelegramChatId(Long chatId) {
        return usersRepo.findByTelegramChatId(chatId); // нужно добавить поле chatId в Users
    }

     @Transactional
    public void updateEmail(Long chatId, String newEmail) {
        usersRepo.findByTelegramChatId(chatId).ifPresent(user -> {
            user.setGmail(newEmail);
            usersRepo.save(user);
        });
    }

    @Transactional
    public void updatePhone(Long chatId, String newPhone) {
        usersRepo.findByTelegramChatId(chatId).ifPresent(user -> {
            user.setPhone(newPhone);
            usersRepo.save(user);
        });
    }

    public Optional<Users> linkTelegramAccount(Long chatId, String email, String name) {
        return usersRepo.findByGmailAndName(email, name)
                .map(user -> {
                    user.setTelegramChatId(chatId);
                    return usersRepo.save(user);
                });
    }


    public void updatePasswordByTelegramChatId(Long chatId, String newPassword){
        usersRepo.findByTelegramChatId(chatId).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepo.save(user);
        });
    }



}



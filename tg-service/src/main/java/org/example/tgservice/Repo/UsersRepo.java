package org.example.tgservice.Repo;

import org.example.tgservice.BD.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {

     Optional<Users> findByGmail(String email);

     Optional<Users> findByUserName(String username);

     Boolean existsUsersByGmail(String gmail);
     Boolean existsUsersByUserName(String name);


     Optional<Users> findByTelegramChatId(Long chatId);
     Optional<Users> findByGmailAndName(String gmail, String name);
}

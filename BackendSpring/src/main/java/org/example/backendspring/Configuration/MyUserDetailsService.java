package org.example.backendspring.Configuration;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.UserRole;
import org.example.backendspring.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private  UsersRepo usersRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepo.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return MyUserDetails.build(user);
    }


    /** Если есть пользователь с таким email (gmail), возвращаем его, иначе создаём новый. */
    @Transactional
    public Users findOrCreateByEmail(String email) {
        String username = email.split("@")[0];

        String givenName= email.split("@")[0];
        String familyName = email.split("@")[0];
        return usersRepo.findByGmail(email)
                .orElseGet(() -> {
                    // 1) Создаём Users
                    Users newUser = new Users();
                    newUser.setGmail(email);
                    newUser.setUserName(username);
                    newUser.setPassword("");         // OAuth-пользователь
                    newUser.setName(givenName);
                    newUser.setSurname(familyName);
                    newUser.setRole(UserRole.user);
                    Users savedUser = usersRepo.save(newUser);
                    return savedUser;
                });
    }
}



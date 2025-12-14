package org.example.backendspring.Service;

import org.example.backendspring.Dto.UserDto;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceConvertDto {


    public final UsersRepo usersRepo;


    @Autowired
    public ServiceConvertDto(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    public UserDto convertDtoUser(Users users){
        return new UserDto(users.getGmail(),
                users.getUserName(),
                users.getGender(),
                users.getName(),
                users.getSurname(),
                users.getPhone());
    }
}

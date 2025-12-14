package org.example.backendspring.Repository;


import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepo extends JpaRepository<Users, Long> {

     Optional<Users> findByGmail(String email);

     Optional<Users> findByUserName(String username);

     Boolean existsUsersByGmail(String gmail);
     Boolean existsUsersByUserName(String name);

}

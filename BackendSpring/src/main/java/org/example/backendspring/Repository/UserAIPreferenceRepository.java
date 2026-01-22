package org.example.backendspring.Repository;

import org.example.backendspring.Entity.UserAIPreference;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAIPreferenceRepository extends JpaRepository<UserAIPreference,Long> {

    List<UserAIPreference> findByUserAndActiveTrue(Users user);

    @Query("select p from UserAIPreference p where p.user.user_id = :userId and p.active = true order by p.createdAt desc")
    List<UserAIPreference> findActiveByUserId(@Param("userId") Long userId);


    List<UserAIPreference> findByUser(Users user);

    @Query("select p from UserAIPreference p where p.user.user_id = :userId")
    List<UserAIPreference> findAllByUserId(@Param("userId") Long userId);
}

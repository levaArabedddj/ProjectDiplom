package org.example.backendspring;

import jakarta.transaction.Transactional;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.UserRole;
import org.example.backendspring.Repository.TripsRepo;
import org.example.backendspring.Repository.UsersRepo;
import org.example.backendspring.Service.TripsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
@SpringBootTest
@ActiveProfiles("test") // <--- ВОТ ГЛАВНОЕ ИЗМЕНЕНИЕ
@Transactional
public class TripsTest {
    @Autowired
    private UsersRepo usersRepo;

    @Autowired
    private TripsRepo tripsRepo;

    @Autowired
    private TripsService tripsService;

    private Users ownerUser;
    private Users strangerUser;
    private Trip ownersTrip;


    @BeforeEach
    void setUp() {
        // 1. Створюємо "Власника" (User A)
        ownerUser = new Users();
        ownerUser.setGmail("owner@test.com");
        ownerUser.setPassword("securepass");
        ownerUser.setRole(UserRole.user);
        usersRepo.save(ownerUser); // Зберігаємо, щоб отримати ID

        // 2. Створюємо "Чужинця" (User B)
        strangerUser = new Users();
        strangerUser.setGmail("stranger@test.com");
        strangerUser.setPassword("securepass");
        strangerUser.setRole(UserRole.user);
        usersRepo.save(strangerUser);

        // 3. Створюємо Поїздку і прив'язуємо до Власника
        ownersTrip = new Trip();
        ownersTrip.setUser(ownerUser); // Важливо: зв'язок з User A
        ownersTrip.setCityName("Paris");
        ownersTrip.setStartDate(LocalDate.now());
        ownersTrip.setEndDate(LocalDate.now().plusDays(5));
        tripsRepo.save(ownersTrip);
    }

    @Test
    void shouldReturnTrip_WhenUserIsOwner() {
        TripDto result = tripsService.getTripById(ownerUser.getUser_id(), ownersTrip.getId());

        assertThat(result).isNotNull();
        assertThat(result.getCityName()).isEqualTo("Paris");
        assertThat(result.getId()).isEqualTo(ownersTrip.getId());
    }

    @Test
    void shouldThrowException_WhenUserIsNotOwner() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tripsService.getTripById(strangerUser.getUser_id(), ownersTrip.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Эта поездка не принадлежит пользователю");
    }

    @Test
    void shouldThrowException_WhenTripNotFound() {
        Long nonExistentTripId = 99999L;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tripsService.getTripById(ownerUser.getUser_id(), nonExistentTripId);
        });

        assertThat(exception.getMessage()).isEqualTo("Поездка не найдена");
    }
}

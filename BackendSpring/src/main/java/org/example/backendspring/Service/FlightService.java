package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Dto.DtoAmadeus.Fly.BookingListResponse;
import org.example.backendspring.Entity.Booking;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Repository.BookingRepo;
import org.example.backendspring.Repository.UsersRepo;
import org.example.backendspring.ServiceApi.AmadeusClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {

    private final UsersRepo usersRepo;
    private final AmadeusClient amadeusClient;
    private final BookingRepo bookingRepo;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(FlightService.class);
    @Autowired
    public FlightService(UsersRepo usersRepo, AmadeusClient amadeusClient, BookingRepo bookingRepo, ObjectMapper objectMapper) {
        this.usersRepo = usersRepo;
        this.amadeusClient = amadeusClient;
        this.bookingRepo = bookingRepo;
        this.objectMapper = objectMapper;
    }


    // 1. Получить все бронирования конкретного пользователя
    public List<BookingListResponse> getUserBookings(Long user) {
        Users users = usersRepo.findById(user).orElseThrow(()->new UsernameNotFoundException("User not found"));
       List<Booking> booking =  bookingRepo.findAllByUser(users);
        return booking.stream().map(booking1 ->
            new BookingListResponse(booking1.getId(),
                    booking1.getPnrReference(),
                    booking1.getTotalPrice(),
                    booking1.getCurrency(),
                    booking1.getStatus(),
                    booking1.getCreatedAt())
        ).toList();

    }

    // 2. Получить ОЧЕНЬ детальную информацию о конкретной брони по ID
    public JsonNode getBookingDetails(Long bookingId, Long user) {
        Users users =  usersRepo.findById(user).orElseThrow(()->new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Бронирование не найдено"));

        if (!booking.getUser().getUser_id().equals(users.getUser_id())) {
            throw new RuntimeException("У вас нет прав на просмотр этого бронирования");
        }

        try {
            return objectMapper.readTree(booking.getFullJsonData());
        } catch (Exception e) {
            log.error("Ошибка парсинга деталей из БД", e);
            throw new RuntimeException("Не удалось прочитать детали рейса");
        }
    }

}

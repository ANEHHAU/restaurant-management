package com.restaurant.management.service;

import com.restaurant.management.entity.*;
import com.restaurant.management.repository.BookingRepository;
import com.restaurant.management.repository.RestaurantTableRepository;
import com.restaurant.management.repository.TableCalendarRepository;
import com.restaurant.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BookingService {

    private final UserRepository userRepo;
    private final RestaurantTableRepository tableRepo;
    private final BookingRepository bookingRepo;
    private final TableCalendarRepository calendarRepo;

    public BookingService(UserRepository userRepo,
                          RestaurantTableRepository tableRepo,
                          BookingRepository bookingRepo,
                          TableCalendarRepository calendarRepo) {
        this.userRepo = userRepo;
        this.tableRepo = tableRepo;
        this.bookingRepo = bookingRepo;
        this.calendarRepo = calendarRepo;
    }

    // ===============================
    // TÌM KHÁCH CŨ
    // ===============================
    public Optional<User> findOldCustomer(String contact){
        return userRepo.findByPhoneOrEmail(contact, contact);
    }

    // ===============================
    // TẠO KHÁCH MỚI
    // ===============================
    public User createNewCustomer(String name, String phone, String email){
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setEmail(email);
        user.setUserType(UserType.Customer);
        return userRepo.save(user);
    }

    // ===============================
    // TÌM BÀN TRỐNG
    // ===============================
    public List<RestaurantTable> findAvailableTables(
            int guests,
            LocalDateTime time){

        return tableRepo.findAvailableTables(
                guests,
                time,
                time.plusHours(2));
    }

    // ===============================
    // ĐẶT BÀN
    // ===============================
    public Booking createBooking(
            User user,
            Integer tableId,
            LocalDateTime time,
            int guests,
            String note){

        RestaurantTable table = tableRepo.findById(tableId)
                .orElseThrow();

        Booking booking = new Booking();
        booking.setCustomer(user);
        booking.setTable(table);
        booking.setBookingTime(time);
        booking.setNumGuests(guests);
        booking.setNote(note);
        booking.setBookingCode(UUID.randomUUID().toString().substring(0,8));
        booking.setStatus(BookingStatus.Reserved);

        bookingRepo.save(booking);

        // Tạo lịch bàn
        TableCalendar calendar = new TableCalendar();
        calendar.setTable(table);
        calendar.setStartTime(time);
        calendar.setEndTime(time.plusHours(2));
        calendar.setStatus(TableCalendar.CalendarStatus.Reserved);

        calendarRepo.save(calendar);

        return booking;
    }
}
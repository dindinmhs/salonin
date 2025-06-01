package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.Service;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.BookingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public Booking createBooking(User customer, User employee, Service service, LocalDate date, LocalTime time) {
        if (customer.getBalance() < service.getPrice()) {
            throw new RuntimeException("Insufficient balance");
        }
        
        Booking booking = new Booking();
        booking.setUser(customer);
        booking.setEmployee(employee);
        booking.setService(service);
        booking.setBranch(service.getBranch()); // Set branch from service
        booking.setDate(date);
        booking.setTime(time);
        booking.setStatus(Booking.Status.PENDING);
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking updateBookingStatus(Long bookingId, Booking.Status newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(newStatus);
        
        if (newStatus == Booking.Status.SELESAI) {
            User customer = booking.getUser();
            customer.setBalance(customer.getBalance() - booking.getService().getPrice());
            userService.updateUser(customer);  // Changed from save() to updateUser()
        }
        
        return bookingRepository.save(booking);
    }
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByCustomer(User customer) {
        // Gunakan 'user' karena field di entity bernama 'user', bukan 'customer'
        return bookingRepository.findByUserOrderByCreatedAtDesc(customer);
    }

    public List<Booking> getBookingsByEmployee(User employee) {
        return bookingRepository.findByEmployeeOrderByDateDesc(employee);
    }

    // Alternative methods with simpler queries
    public List<Booking> getAllBookingsSimple() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByCustomerSimple(User customer) {
        return bookingRepository.findByUser(customer);  // 'user' field
    }

    public List<Booking> getBookingsByEmployeeSimple(User employee) {
        return bookingRepository.findByEmployee(employee);
    }
}
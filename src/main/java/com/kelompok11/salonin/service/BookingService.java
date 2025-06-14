package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.Service;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.BookingRepository;
import com.kelompok11.salonin.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
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
    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.Status.valueOf(status));
        booking = bookingRepository.save(booking);
        
        if ("SELESAI".equals(status)) {
            User customer = booking.getUser();
            Integer newBalance = customer.getBalance() - booking.getService().getPrice();
            customer.setBalance(newBalance);
            
            // PENTING: Jangan panggil updateUser, langsung save ke repository
            // untuk menghindari enkripsi ulang password
            userRepository.save(customer);
        }
        
        return booking;
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

    public Double getTotalRevenueThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        List<Booking> bookings = bookingRepository.findByDateBetweenAndStatus(
            startOfMonth, endOfMonth, Booking.Status.SELESAI);
        
        return bookings.stream()
            .mapToDouble(booking -> booking.getService().getPrice())
            .sum();
    }
    
    public Long getBookingCountThisMonth() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        return bookingRepository.countByDateBetween(startOfMonth, endOfMonth);
    }

    public List<Booking> getPendingBookingsByEmployee(User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        return bookingRepository.findByEmployeeAndStatusOrderByDateAscTimeAsc(user, Booking.Status.PENDING);
    }

    public List<Booking> getBookingsByUser(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBookingsByUser'");
    }
}
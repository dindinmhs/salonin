package com.kelompok11.salonin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Existing methods that were working
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByEmployeeAndStatusOrderByDateAscTimeAsc(User employee, Booking.Status status);
    List<Booking> findByStatusOrderByDateAscTimeAsc(Booking.Status status);
    
    // New methods using correct field names
    List<Booking> findByUser(User user);  // user is the customer field
    List<Booking> findByEmployee(User employee);
    List<Booking> findByStatus(Booking.Status status);
    List<Booking> findByUserAndStatus(User user, Booking.Status status);
    
    // Ordering methods using correct field names
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByUserOrderByDateDescTimeDesc(User user);
    List<Booking> findByEmployeeOrderByDateDescTimeDesc(User employee);
    
    // Alternative simpler ordering
    List<Booking> findAllByOrderByDateDescTimeAsc();
    List<Booking> findByUserOrderByDateDesc(User user);
    List<Booking> findByEmployeeOrderByDateDesc(User employee);
}
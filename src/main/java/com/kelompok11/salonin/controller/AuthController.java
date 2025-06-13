package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.BookingService;
import com.kelompok11.salonin.service.NotificationsService;
import com.kelompok11.salonin.service.ReviewService;
import com.kelompok11.salonin.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final BookingService bookingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private NotificationsService notificationsService;
    
    @Autowired
    private com.kelompok11.salonin.service.BranchService branchService;
    
    public AuthController(BookingService bookingService) {
        this.bookingService = bookingService;

    }
    
    @GetMapping("/")
    public String landingPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // User sudah login, tambahkan data user ke model
            String username = authentication.getName();
            User user = userService.findByEmail(username);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("isAuthenticated", true);
                
                // Tambahkan role user
                String role = user.getRole().name();
                model.addAttribute("userRole", role);
                
                // Tambahkan notifikasi count
                long unreadCount = notificationsService.countUnreadNotifications(user);
                model.addAttribute("unreadNotificationsCount", unreadCount);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }
        
        // Tambahkan data branch untuk ditampilkan di homepage
        List<com.kelompok11.salonin.model.Branch> branches = branchService.getAllBranches();
        model.addAttribute("branches", branches);
        
        return "index";
    }
    
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Set role sebagai CUSTOMER
            user.setRole(User.Role.CUSTOMER);
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
    
    


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole().toString());
        
        // Add unread notifications count
        long unreadNotificationsCount = notificationsService.countUnreadNotifications(user);
        model.addAttribute("unreadNotificationsCount", unreadNotificationsCount);

        List<Booking> bookings;
    
        // Different logic based on user role
        if (user.getRole() == User.Role.ADMIN) {
            // Admin can see all bookings
            bookings = bookingService.getAllBookings();
            model.addAttribute("allBookings", bookings);
        } else if (user.getRole() == User.Role.EMPLOYEE) {
            // Employee can see bookings assigned to them
            bookings = bookingService.getBookingsByEmployee(user);
            model.addAttribute("allBookings", bookings);
            // Add pending bookings for employees
            List<Booking> pendingBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.PENDING)
                .collect(Collectors.toList());
            model.addAttribute("pendingBookings", pendingBookings);
            List<Booking> acceptedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.Status.DITERIMA)
                .collect(Collectors.toList());
            model.addAttribute("acceptedBookings", acceptedBookings);
        } else {
            // Customer can only see their own bookings
            bookings = bookingService.getBookingsByCustomer(user);
            model.addAttribute("bookings", bookings); 
            Map<Long, Boolean> reviewExistenceMap = new HashMap<>();
            for (Booking booking : bookings) {
                boolean exists = reviewService.findByBookingId(booking.getId()).isPresent();
                reviewExistenceMap.put(booking.getId(), exists);
            }
            model.addAttribute("reviewExistMap", reviewExistenceMap);
            boolean hasSelesaiBooking = bookings.stream()
                .anyMatch(b -> b.getStatus() == Booking.Status.SELESAI);
            model.addAttribute("hasSelesaiBooking", hasSelesaiBooking);
        }
        
        return "dashboard";
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}

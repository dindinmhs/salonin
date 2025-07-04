package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.UserRepository;
import com.kelompok11.salonin.service.BookingService;
import com.kelompok11.salonin.service.ReviewService;
import com.kelompok11.salonin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/generate-report")
    public ResponseEntity<?> generateReport() {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                // User not logged in, redirect to login
                return ResponseEntity.status(302)
                    .header("Location", "/login")
                    .build();
            }
            
            // Get UserDetails from authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            
            // Find the actual User entity from database
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("User not found in database.");
            }
            
            User user = userOptional.get();
            
            // Check if user is admin
            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Access denied. Only administrators can generate reports.");
            }

            // Generate report content
            StringBuilder report = new StringBuilder();
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            report.append("SALONIN BUSINESS REPORT\n");
            report.append("Generated on: ").append(now.format(formatter)).append("\n");
            report.append("Generated by: ").append(user.getName()).append(" (").append(user.getEmail()).append(")\n\n");
            
            // Business Analytics
            report.append("=== BUSINESS ANALYTICS ===\n");
            
            Double totalRevenue = bookingService.getTotalRevenueThisMonth();
            Long totalBookings = bookingService.getBookingCountThisMonth();
            Long totalCustomers = userService.getCustomerCount();
            Double averageRating = reviewService.getOverallAverageRating();
            
            report.append("Total Revenue This Month: Rp ").append(String.format("%,.0f", totalRevenue != null ? totalRevenue : 0)).append("\n");
            report.append("Total Bookings This Month: ").append(totalBookings != null ? totalBookings : 0).append("\n");
            report.append("Total Customers: ").append(totalCustomers != null ? totalCustomers : 0).append("\n");
            report.append("Average Rating: ").append(String.format("%.1f", averageRating != null ? averageRating : 0.0)).append("/5.0\n\n");
            
            // Monthly Performance
            report.append("=== MONTHLY PERFORMANCE ===\n");
            report.append("Period: ").append(now.withDayOfMonth(1).format(formatter))
                   .append(" - ").append(now.format(formatter)).append("\n");
            
            if (totalBookings != null && totalBookings > 0 && totalRevenue != null) {
                report.append("Average Revenue per Booking: Rp ").append(String.format("%,.0f", totalRevenue / totalBookings)).append("\n");
            } else {
                report.append("Average Revenue per Booking: Rp 0\n");
            }
            
            // Additional Statistics
            report.append("\n=== DETAILED STATISTICS ===\n");
            report.append("Report Period: ").append(now.getMonth().toString()).append(" ").append(now.getYear()).append("\n");
            
            if (totalCustomers != null && totalCustomers > 0) {
                double bookingsPerCustomer = totalBookings != null ? (double) totalBookings / totalCustomers : 0;
                report.append("Average Bookings per Customer: ").append(String.format("%.2f", bookingsPerCustomer)).append("\n");
            }
            
            report.append("\n=== SYSTEM STATUS ===\n");
            report.append("System Status: Operational\n");
            report.append("Database Status: Connected\n");
            report.append("Last Updated: ").append(now.format(formatter)).append("\n");
            
            report.append("\n--- End of Report ---");

            // Return as downloadable text file
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "salonin-report-" + now.toString() + ".txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report.toString());
                    
        } catch (Exception e) {
            // Log the error and return user-friendly message
            e.printStackTrace();
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error generating report: " + e.getMessage());
        }
    }
}
package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.Service;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.BookingService;
import com.kelompok11.salonin.service.BranchService;
import com.kelompok11.salonin.service.NotificationsService;
import com.kelompok11.salonin.service.ServiceService;
import com.kelompok11.salonin.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BranchService branchService;
    
    @Autowired
    private NotificationsService notificationsService;
    
    @PostMapping("/create")
    public String createBooking(@ModelAttribute("bookingRequest") Booking request,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            Booking booking = bookingService.createBooking(user, request.getEmployee(), 
                request.getService(), request.getDate(), request.getTime());
            
            // Kirim notifikasi ke employee yang dipilih
            String title = "Booking Baru";
            String message = "Anda memiliki booking baru dari " + user.getName() + 
                            " untuk layanan " + request.getService().getName() + 
                            " pada tanggal " + request.getDate() + 
                            " pukul " + request.getTime();
            
            notificationsService.createNotification(request.getEmployee(), title, message);
            
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
    
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                             @RequestParam Booking.Status status,
                             RedirectAttributes redirectAttributes) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        if (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.EMPLOYEE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access");
            return "redirect:/dashboard";
        }
        
        try {
            Booking booking = bookingService.updateBookingStatus(id, status);
            
            // Kirim notifikasi ke customer tentang perubahan status
            String title = "Status Booking Diperbarui";
            String statusText = "";
            
            switch(status) {
                case DITERIMA:
                    statusText = "diterima";
                    break;
                case BATAL:
                    statusText = "dibatalkan";
                    break;
                case SELESAI:
                    statusText = "telah selesai";
                    break;
                default:
                    statusText = "diperbarui";
            }
            
            String message = "Booking Anda untuk layanan " + booking.getService().getName() + 
                            " pada tanggal " + booking.getDate() + 
                            " pukul " + booking.getTime() + 
                            " telah " + statusText;
            
            notificationsService.createNotification(booking.getUser(), title, message);
            
            redirectAttributes.addFlashAttribute("successMessage", "Booking status updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dashboard";
    }
    
    @GetMapping("/new")
    public String showBookingForm(Model model) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        // Add user and role to model
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole().toString());
        
        // Get all services for the dropdown
        model.addAttribute("services", serviceService.getAllServices());
        
        // Get all employees for the dropdown
        List<User> employees = userService.findAllByRole(User.Role.EMPLOYEE);
        model.addAttribute("employees", employees);
        
        // Get all branches for the dropdown
        model.addAttribute("branches", branchService.getAllBranches());
        
        // Add empty booking object for form binding
        model.addAttribute("bookingRequest", new Booking());
        
        return "bookings/form";
    }
    
    @GetMapping("/services/{branchId}")
    @ResponseBody
    public List<Service> getServicesByBranch(@PathVariable Long branchId) {
        return serviceService.getServicesByBranch(branchId);
    }

    @GetMapping("/users/{branchId}")
    @ResponseBody
    public List<User> getUsersByBranch(@PathVariable Long branchId) {
        return userService.getUsersByBranch(branchId);
    }
}
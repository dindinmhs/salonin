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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBooking(@ModelAttribute("bookingRequest") Booking request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            // Get complete Service object from database
            Service service = serviceService.getServiceById(request.getService().getId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
            
            // Get available employee from the same branch as the service
            List<User> employees = userService.getEmployeesByBranch(service.getBranch().getId());
            if (employees.isEmpty()) {
                throw new RuntimeException("No employee available for this branch");
            }
            
            // Select the first available employee (you can implement more sophisticated logic here)
            User employee = employees.get(0);
            
            Booking booking = bookingService.createBooking(user, employee, 
                service, request.getDate(), request.getTime());
            
            // Kirim notifikasi ke employee yang dipilih
            String title = "Booking Baru";
            String message = "Anda memiliki booking baru dari " + user.getName() + 
                            " untuk layanan " + service.getName() + 
                            " pada tanggal " + request.getDate() + 
                            " pukul " + request.getTime();
            
            notificationsService.createNotification(employee, title, message);
            
            response.put("success", true);
            response.put("message", "Booking berhasil");
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            if (e.getMessage().contains("Insufficient balance")) {
                response.put("message", "Maaf saldo tidak mencukupi");
                response.put("showTopUp", true);
            } else {
                response.put("message", e.getMessage());
                response.put("showTopUp", false);
            }
            return ResponseEntity.badRequest().body(response);
        }
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
            Booking booking = bookingService.updateBookingStatus(id, status.toString());
            
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
package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Booking;
import com.kelompok11.salonin.model.TopupHistory;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.BookingService;
import com.kelompok11.salonin.service.TopUpService;
import com.kelompok11.salonin.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private TopUpService topUpService;
    
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/profiles/";
    
    @GetMapping
    public String profilePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        // Get booking history
        List<Booking> bookingHistory = bookingService.getBookingsByCustomer(user);
        
        // Get transaction history
        List<TopupHistory> transactionHistory = topUpService.getTopupHistoryByUser(user);
        
        // Calculate statistics
        long completedBookings = bookingHistory.stream()
            .filter(b -> b.getStatus() == Booking.Status.SELESAI)
            .count();
            
        int totalSpent = bookingHistory.stream()
            .filter(b -> b.getStatus() == Booking.Status.SELESAI)
            .mapToInt(b -> b.getService().getPrice())
            .sum();
            
        int totalTopup = transactionHistory.stream()
            .filter(h -> h.getStatus() == TopupHistory.Status.SUKSES)
            .mapToInt(TopupHistory::getAmount)
            .sum();
        
        model.addAttribute("user", user);
        model.addAttribute("bookingHistory", bookingHistory);
        model.addAttribute("transactionHistory", transactionHistory);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("totalTopup", totalTopup);
        
        return "profile/index";
    }
    
    @PostMapping("/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile file, 
                             RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload");
                return "redirect:/profile";
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please upload a valid image file");
                return "redirect:/profile";
            }
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update user profile
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            // Delete old photo if exists
            if (user.getImgUrl() != null && !user.getImgUrl().isEmpty()) {
                try {
                    Path oldFilePath = Paths.get(UPLOAD_DIR + user.getImgUrl().replace("/uploads/profiles/", ""));
                    Files.deleteIfExists(oldFilePath);
                } catch (Exception e) {
                    // Log error but don't fail the upload
                }
            }
            
            user.setImgUrl("/uploads/profiles/" + filename);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Profile photo updated successfully!");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload photo: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/delete-photo")
    public String deletePhoto(RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            if (user.getImgUrl() != null && !user.getImgUrl().isEmpty()) {
                // Delete file from filesystem
                try {
                    Path filePath = Paths.get(UPLOAD_DIR + user.getImgUrl().replace("/uploads/profiles/", ""));
                    Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    // Log error but continue
                }
                
                // Update user profile
                user.setImgUrl(null);
                userService.updateUser(user);
                
                redirectAttributes.addFlashAttribute("successMessage", "Profile photo deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No profile photo to delete");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete photo: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
    
    @PostMapping("/update-name")
    public String updateName(@RequestParam("name") String name, 
                            RedirectAttributes redirectAttributes) {
        try {
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Name cannot be empty");
                return "redirect:/profile";
            }
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email);
            
            user.setName(name.trim());
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Name updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update name: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }
}
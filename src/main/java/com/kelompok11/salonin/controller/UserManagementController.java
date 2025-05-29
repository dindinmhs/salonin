package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {
    
    @Autowired
    private UserService userService;
    
    private final String uploadDir = "src/main/resources/static/images/users/";
    
    // Check if user is admin before any action
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        return user != null && user.getRole() == User.Role.ADMIN;
    }
    
    @GetMapping
    public String listUsers(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/list";
    }
    
    @GetMapping("/add")
    public String showAddUserForm(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        return "admin/users/form";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        return "admin/users/form";
    }
    
    @PostMapping
    public String createOrUpdateUser(@ModelAttribute User user, @RequestParam("imageFile") MultipartFile imageFile, 
                                   RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            // Handle image upload if provided
            if (!imageFile.isEmpty()) {
                // Create directory if it doesn't exist
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                
                // Generate unique filename
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID().toString() + fileExtension;
                
                // Save file
                Path filePath = uploadPath.resolve(newFilename);
                Files.copy(imageFile.getInputStream(), filePath);
                
                // Set image URL in user object
                user.setImgUrl("/images/users/" + newFilename);
            }
            
            // Save or update user
            if (user.getId() == null) {
                // New user
                userService.createUserWithRole(user.getName(), user.getEmail(), user.getPassword(), user.getRole());
                redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            } else {
                // Existing user
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            }
            
            return "redirect:/admin/users";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading image: " + e.getMessage());
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }
    
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}
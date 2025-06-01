package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.UserService;
import com.kelompok11.salonin.service.BranchService;
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
    
    @Autowired
    private BranchService branchService;
    
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
        model.addAttribute("branches", branchService.getAllBranches());
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
        
        // Only allow editing EMPLOYEE users
        if (user.getRole() != User.Role.EMPLOYEE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Can only edit staff members");
            return "redirect:/admin/users";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("branches", branchService.getAllBranches());
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
            // Force role to EMPLOYEE
            user.setRole(User.Role.EMPLOYEE);
            
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
                userService.createUserWithRole(user.getName(), user.getEmail(), user.getPassword(), User.Role.EMPLOYEE);
                redirectAttributes.addFlashAttribute("successMessage", "Staff member created successfully!");
            } else {
                // Verify user is an EMPLOYEE before updating
                User existingUser = userService.getUserById(user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + user.getId()));
                if (existingUser.getRole() != User.Role.EMPLOYEE) {
                    throw new RuntimeException("Can only update staff members");
                }
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("successMessage", "Staff member updated successfully!");
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
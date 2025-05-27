package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Branch;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.BranchService;
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

@Controller
@RequestMapping("/admin/branches")
public class BranchController {
    
    @Autowired
    private BranchService branchService;
    
    @Autowired
    private UserService userService;
    
    // Check if user is admin before any action
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        return user != null && user.getRole() == User.Role.ADMIN;
    }
    
    @GetMapping
    public String listBranches(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/branches/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("branch", new Branch());
        return "admin/branches/form";
    }
    
    @PostMapping
    public String createBranch(@ModelAttribute Branch branch, 
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            branchService.saveBranch(branch, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Branch created successfully!");
            return "redirect:/admin/branches";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading image: " + e.getMessage());
            return "redirect:/admin/branches/new";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        Branch branch = branchService.getBranchById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        model.addAttribute("branch", branch);
        return "admin/branches/form";
    }
    
    @PostMapping("/update/{id}")
    public String updateBranch(@PathVariable Long id, 
                              @ModelAttribute Branch branch,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            branchService.updateBranch(id, branch, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Branch updated successfully!");
            return "redirect:/admin/branches";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error uploading image: " + e.getMessage());
            return "redirect:/admin/branches/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteBranch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            branchService.deleteBranch(id);
            redirectAttributes.addFlashAttribute("successMessage", "Branch deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting branch: " + e.getMessage());
        }
        return "redirect:/admin/branches";
    }
}
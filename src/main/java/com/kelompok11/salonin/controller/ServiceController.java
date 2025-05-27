package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Service;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.ServiceService;
import com.kelompok11.salonin.service.BranchService;
import com.kelompok11.salonin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
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
    public String listServices(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("services", serviceService.getAllServices());
        return "admin/services/list";
    }
    
    @GetMapping("/new")
    public String showCreateForm(Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        model.addAttribute("service", new Service());
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/services/form";
    }
    
    @PostMapping
    public String createService(@ModelAttribute Service service, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            serviceService.saveService(service);
            redirectAttributes.addFlashAttribute("successMessage", "Service created successfully!");
            return "redirect:/admin/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating service: " + e.getMessage());
            return "redirect:/admin/services/new";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        Service service = serviceService.getServiceById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        model.addAttribute("service", service);
        model.addAttribute("branches", branchService.getAllBranches());
        return "admin/services/form";
    }
    
    @PostMapping("/update/{id}")
    public String updateService(@PathVariable Long id, @ModelAttribute Service service, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            serviceService.updateService(id, service);
            redirectAttributes.addFlashAttribute("successMessage", "Service updated successfully!");
            return "redirect:/admin/services";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating service: " + e.getMessage());
            return "redirect:/admin/services/edit/" + id;
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access denied. Admin privileges required.");
            return "redirect:/dashboard";
        }
        
        try {
            serviceService.deleteService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting service: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }
}
package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Branch;
import com.kelompok11.salonin.model.Review;
import com.kelompok11.salonin.model.Service;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.BranchService;
import com.kelompok11.salonin.service.ServiceService;
import com.kelompok11.salonin.service.UserService;
import com.kelompok11.salonin.service.NotificationsService;
import com.kelompok11.salonin.service.ReviewService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/branches")
public class UserBranchController {
    
    @Autowired
    private BranchService branchService;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationsService notificationsService;
    
    @Autowired
    private ReviewService reviewService;
    
    // Helper method to add user info to model
    private void addUserInfoToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userRole", user.getRole().name());
                
                long unreadCount = notificationsService.countUnreadNotifications(user);
                model.addAttribute("unreadNotificationsCount", unreadCount);
            }
        } else {
            model.addAttribute("isAuthenticated", false);
        }
    }
    
    @GetMapping
    public String listBranches(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "city", required = false) String city,
                              @RequestParam(value = "province", required = false) String province,
                              Model model) {
        
        addUserInfoToModel(model);
        
        List<Branch> branches;
        
        if (search != null && !search.trim().isEmpty()) {
            branches = branchService.searchBranches(search);
            model.addAttribute("searchKeyword", search);
        } else if (city != null && !city.trim().isEmpty()) {
            branches = branchService.getBranchesByCity(city);
            model.addAttribute("selectedCity", city);
        } else if (province != null && !province.trim().isEmpty()) {
            branches = branchService.getBranchesByProvince(province);
            model.addAttribute("selectedProvince", province);
        } else {
            branches = branchService.getAllBranches();
        }
        
        model.addAttribute("branches", branches);
        
        // Get unique cities and provinces for filter dropdown
        List<String> cities = branchService.getAllBranches().stream()
                .map(Branch::getCity)
                .distinct()
                .sorted()
                .toList();
        List<String> provinces = branchService.getAllBranches().stream()
                .map(Branch::getProvince)
                .distinct()
                .sorted()
                .toList();
                
        model.addAttribute("cities", cities);
        model.addAttribute("provinces", provinces);
        
        return "branches/list";
    }
    
    @GetMapping("/{id}")
    public String branchDetail(@PathVariable Long id, Model model) {
        addUserInfoToModel(model);
        
        Optional<Branch> branchOpt = branchService.getBranchById(id);
        if (branchOpt.isEmpty()) {
            return "redirect:/branches?error=notfound";
        }
        
        Branch branch = branchOpt.get();
        List<Service> services = serviceService.getServicesByBranch(id);
        List<User> employees = userService.getEmployeesByBranch(id);
        
        // Tambahkan rating data
        Double averageRating = reviewService.getBranchAverageRating(id);
        Long reviewCount = reviewService.getBranchReviewCount(id);
        
        // Tambahkan review data
        List<Review> reviews = reviewService.getReviewsByBranch(id);
        
        model.addAttribute("branch", branch);
        model.addAttribute("services", services);
        model.addAttribute("employees", employees);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reviews", reviews);
        
        return "branches/detail";
    }
}
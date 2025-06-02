package com.kelompok11.salonin.controller;

import com.kelompok11.salonin.model.Notifications;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.service.NotificationsService;
import com.kelompok11.salonin.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationsController {
    
    @Autowired
    private NotificationsService notificationsService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String showNotifications(Model model) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        List<Notifications> notifications = notificationsService.getNotificationsByUser(user);
        
        model.addAttribute("user", user);
        model.addAttribute("userRole", user.getRole().toString());
        model.addAttribute("notifications", notifications);
        
        return "notifications/list";
    }
    
    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        long count = notificationsService.countUnreadNotifications(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    // Menghapus endpoint API yang tidak diperlukan
    // @GetMapping("/api/list")
    // @ResponseBody
    // public ResponseEntity<List<Notifications>> getNotifications() { ... }
    
    // Mengubah endpoint mark as read menjadi form submission
    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        notificationsService.markAsRead(id);
        redirectAttributes.addFlashAttribute("success", "Notifikasi telah ditandai sebagai dibaca");
        return "redirect:/notifications";
    }
    
    // Mengubah endpoint mark all as read menjadi form submission
    @PostMapping("/read-all")
    public String markAllAsRead(RedirectAttributes redirectAttributes) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email);
        
        notificationsService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute("success", "Semua notifikasi telah ditandai sebagai dibaca");
        return "redirect:/notifications";
    }
}
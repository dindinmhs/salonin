package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }
        
        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Save user
        return userRepository.save(user);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // Method untuk membuat user dengan role tertentu
    public User createUserWithRole(String name, String email, String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered!");
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUser(User user) {
        // Check if email already exists and it's not the same user
        if (userRepository.existsByEmail(user.getEmail()) && 
            !userRepository.findByEmail(user.getEmail()).get().getId().equals(user.getId())) {
            throw new RuntimeException("Email already registered by another user!");
        }
        
        // If password is empty, it means we don't want to update it
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // Get current password from database
            User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found!"));
            user.setPassword(existingUser.getPassword());
        } else {
            // Encrypt new password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    // Add this method to UserService
    public List<User> findAllByRole(User.Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }
    public List<User> getUsersByBranch(Long branchId) {
        return userRepository.findByBranchId(branchId);
    }
}
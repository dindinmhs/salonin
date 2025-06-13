package com.kelompok11.salonin.config;

import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        seedUsers();
    }
    
    private void seedUsers() {
        // Create Admin account if not exists
        if (!userRepository.existsByEmail("admin@seniora.com")) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@seniora.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin account created: admin@seniora.com / admin123");
        }

        
        if (!userRepository.existsByEmail("admin2@seniora.com")) {
            User admin = new User();
            admin.setName("Administrator2");
            admin.setEmail("admin2@seniora.com");
            admin.setPassword(passwordEncoder.encode("admin1234"));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin account created: admin2@seniora.com / admin1234");
        }
        
        // Create Employee account if not exists
        if (!userRepository.existsByEmail("employee@seniora.com")) {
            User employee = new User();
            employee.setName("Employee Seniora");
            employee.setEmail("employee@seniora.com");
            employee.setPassword(passwordEncoder.encode("employee123"));
            employee.setRole(User.Role.EMPLOYEE);
            userRepository.save(employee);
            System.out.println("Employee account created: employee@seniora.com / employee123");
        }
        
        // Create additional employees if needed
        if (!userRepository.existsByEmail("sarah@seniora.com")) {
            User employee2 = new User();
            employee2.setName("Sarah Beautician");
            employee2.setEmail("sarah@seniora.com");
            employee2.setPassword(passwordEncoder.encode("sarah123"));
            employee2.setRole(User.Role.EMPLOYEE);
            userRepository.save(employee2);
            System.out.println("Employee account created: sarah@seniora.com / sarah123");
        }
        
        if (!userRepository.existsByEmail("maya@seniora.com")) {
            User employee3 = new User();
            employee3.setName("Maya Therapist");
            employee3.setEmail("maya@seniora.com");
            employee3.setPassword(passwordEncoder.encode("maya123"));
            employee3.setRole(User.Role.EMPLOYEE);
            userRepository.save(employee3);
            System.out.println("Employee account created: maya@seniora.com / maya123");
        }

        if (!userRepository.existsByEmail("hen@gmail.com")) {
            User user1 = new User();
            user1.setName("Hendra Darmawan");
            user1.setEmail("hen@gmail.com");
            user1.setPassword(passwordEncoder.encode("hen123"));
            user1.setRole(User.Role.CUSTOMER);
            userRepository.save(user1);
            System.out.println("User account created: hen@gmail.com / hen123");
        }
    }
}
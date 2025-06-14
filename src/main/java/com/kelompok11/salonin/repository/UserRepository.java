package com.kelompok11.salonin.repository;
import com.kelompok11.salonin.model.User;
import com.kelompok11.salonin.model.User.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByBranchId(Long branchId);
    Long countByRole(Role customer);
}

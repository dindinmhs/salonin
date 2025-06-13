package com.kelompok11.salonin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kelompok11.salonin.model.Branch;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>{
    
    // Search branches by name, city, or province
    @Query("SELECT b FROM Branch b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.province) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Branch> searchBranches(@Param("keyword") String keyword);
    
    // Find branches by city
    List<Branch> findByCityContainingIgnoreCase(String city);
    
    // Find branches by province
    List<Branch> findByProvinceContainingIgnoreCase(String province);
}

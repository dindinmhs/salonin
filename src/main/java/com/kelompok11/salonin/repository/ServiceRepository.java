package com.kelompok11.salonin.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kelompok11.salonin.model.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long>{

List<Service> findByBranchId(Long branchId);
}

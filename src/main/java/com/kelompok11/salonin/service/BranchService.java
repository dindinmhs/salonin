package com.kelompok11.salonin.service;

import com.kelompok11.salonin.model.Branch;
import com.kelompok11.salonin.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BranchService {
    
    @Autowired
    private BranchRepository branchRepository;
    
    private final String uploadDir = "src/main/resources/static/images/branches/";
    
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
    
    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }
    
    public Branch saveBranch(Branch branch, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            branch.setImgUrl(imageUrl);
        }
        return branchRepository.save(branch);
    }
    
    public Branch updateBranch(Long id, Branch branchDetails, MultipartFile imageFile) throws IOException {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        
        branch.setName(branchDetails.getName());
        branch.setAddress(branchDetails.getAddress());
        branch.setProvince(branchDetails.getProvince());
        branch.setCity(branchDetails.getCity());
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            branch.setImgUrl(imageUrl);
        }
        
        return branchRepository.save(branch);
    }
    
    public void deleteBranch(Long id) {
        branchRepository.deleteById(id);
    }
    
    private String saveImage(MultipartFile file) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // Return URL path
        return "/images/branches/" + filename;
    }
}
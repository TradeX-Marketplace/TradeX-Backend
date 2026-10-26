package com.kalibyte.tradex.auth.repository;

import com.kalibyte.tradex.auth.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    Optional<Admin> findByEmail(String email);
    
    Optional<Admin> findByAdminCode(String adminCode);
    
    Optional<Admin> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(a) FROM Admin a")
    long countAllAdmins();
}

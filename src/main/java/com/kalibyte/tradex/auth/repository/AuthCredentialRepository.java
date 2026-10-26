package com.kalibyte.tradex.auth.repository;

import com.kalibyte.tradex.auth.entity.AuthCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredential, Long> {
    Optional<AuthCredential> findByPartyId(Long partyId);
    Optional<AuthCredential> findByEmail(String email);
    Optional<AuthCredential> findByPhone(String phone);
    Optional<AuthCredential> findByEmailOrPhone(String email, String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}

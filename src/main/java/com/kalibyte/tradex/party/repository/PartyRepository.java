package com.kalibyte.tradex.party.repository;

import com.kalibyte.tradex.party.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    Optional<Party> findByPartyCode(String partyCode);
    
    Optional<Party> findByGstin(String gstin);
    
    Optional<Party> findByEmail(String email);
    
    Optional<Party> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);

    Optional<Party> findByIdAndIsDeletedFalse(Long id);

    Optional<Party> findByPartyCodeAndIsDeletedFalse(String partyCode);

    org.springframework.data.domain.Page<Party> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);

    org.springframework.data.domain.Page<Party> findByIsDeletedTrue(org.springframework.data.domain.Pageable pageable);

    java.util.List<Party> findByIsDeletedFalse();
    
    @Query("SELECT COUNT(p) FROM Party p WHERE p.isDeleted = false")
    long countAllParties();

    @Query("SELECT COUNT(p) FROM Party p")
    long countTotalParties();
}

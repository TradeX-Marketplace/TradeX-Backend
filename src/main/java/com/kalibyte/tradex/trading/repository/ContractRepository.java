package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    Optional<Contract> findByContractNumber(String contractNumber);
    
    Optional<Contract> findByDealId(Long dealId);
}

package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Supply;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
    
    Optional<Supply> findBySupplyNumber(String supplyNumber);
    
    /**
     * Pessimistic locking for inventory deduction during deal lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Supply s WHERE s.id = :id")
    Optional<Supply> findByIdWithLock(@Param("id") Long id);
    
    /**
     * Find available supplies for a commodity
     */
    List<Supply> findByCommodityIdAndStatusOrderByAskingRateAsc(Long commodityId, String status);
    
    /**
     * Paginated available supplies by commodity
     */
    Page<Supply> findByCommodityIdAndStatusOrderByCreatedAtDesc(Long commodityId, String status, Pageable pageable);
    
    /**
     * Find supplies by party and status
     */
    List<Supply> findByPartyIdAndStatusOrderByCreatedAtDesc(Long partyId, String status);
    
    /**
     * Find supplies expiring soon
     */
    List<Supply> findByValidTillBetweenAndStatusOrderByValidTillAsc(LocalDate start, LocalDate end, String status);
    
    /**
     * Check availability
     */
    boolean existsBySupplyNumberAndStatus(String supplyNumber, String status);
}

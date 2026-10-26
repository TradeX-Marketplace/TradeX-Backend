package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    
    Optional<Deal> findByDealNumber(String dealNumber);
    
    /**
     * Find deals by status
     */
    List<Deal> findByStatusOrderByCreatedAtDesc(String status);
    
    Page<Deal> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * Find unlocked deals (admin operations)
     */
    List<Deal> findByLockStatusOrderByCreatedAtDesc(String lockStatus);
    
    /**
     * Deals involving a party
     */
    @Query("""
        SELECT d FROM Deal d 
        WHERE (d.buyerParty.id = :partyId OR d.sellerParty.id = :partyId)
        ORDER BY d.createdAt DESC
        """)
    Page<Deal> findByParty(@Param("partyId") Long partyId, Pageable pageable);
    
    /**
     * Sum of locked quantity for a supply
     */
    @Query("""
        SELECT COALESCE(SUM(d.quantity), 0) FROM Deal d 
        WHERE d.supply.id = :supplyId 
        AND d.lockStatus = 'LOCKED'
        """)
    BigDecimal sumLockedQuantityBySupply(@Param("supplyId") Long supplyId);
    
    /**
     * Find matched deals awaiting admin lock
     */
    List<Deal> findByStatusAndLockStatus(String status, String lockStatus);
    
    /**
     * Find all buyer's deals
     */
    List<Deal> findByBuyerPartyIdOrderByCreatedAtDesc(Long buyerPartyId);
    
    /**
     * Find all seller's deals
     */
    List<Deal> findBySellerPartyIdOrderByCreatedAtDesc(Long sellerPartyId);
}

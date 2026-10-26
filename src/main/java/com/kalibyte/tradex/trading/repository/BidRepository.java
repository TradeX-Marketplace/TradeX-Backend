package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    Optional<Bid> findByBidNumber(String bidNumber);
    
    /**
     * Find bids on a supply by status
     */
    List<Bid> findBySupplyIdAndStatusOrderByBidRateDesc(Long supplyId, String status);
    
    /**
     * Find bids on a demand
     */
    List<Bid> findByDemandIdAndStatusOrderByCreatedAtDesc(Long demandId, String status);
    
    /**
     * Find party's recent bids
     */
    Page<Bid> findByPartyIdOrderByCreatedAtDesc(Long partyId, Pageable pageable);
    
    /**
     * Find pending bids awaiting counter-offer
     */
    List<Bid> findByStatusOrderByCreatedAtAsc(String status);
    
    /**
     * Check bid status
     */
    Optional<Bid> findByIdAndStatus(Long id, String status);
    
    /**
     * Highest bid on supply
     */
    @Query("""
        SELECT b FROM Bid b 
        WHERE b.supply.id = :supplyId 
        AND b.status IN ('PENDING', 'COUNTERED', 'ACCEPTED')
        ORDER BY b.bidRate DESC 
        LIMIT 1
        """)
    Optional<Bid> findHighestBidOnSupply(@Param("supplyId") Long supplyId);
    
    /**
     * Get all bids with pagination (ordered by creation date - newest first)
     */
    @Query("SELECT b FROM Bid b ORDER BY b.createdAt DESC")
    Page<Bid> findAllBidsOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Get bids for a specific date
     */
    @Query("""
        SELECT b FROM Bid b 
        WHERE DATE(b.createdAt) = DATE(:date)
        ORDER BY b.createdAt DESC
        """)
    List<Bid> findBidsByDate(@Param("date") LocalDateTime date);
}

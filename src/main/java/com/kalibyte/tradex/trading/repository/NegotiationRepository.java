package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Negotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
    
    Optional<Negotiation> findByNegotiationCode(String negotiationCode);
    
    /**
     * Find negotiations involving a party
     */
    @Query("""
        SELECT n FROM Negotiation n 
        WHERE (n.buyerParty.id = :partyId OR n.sellerParty.id = :partyId)
        AND n.status = :status
        ORDER BY n.updatedAt DESC
        """)
    Page<Negotiation> findByPartyAndStatus(@Param("partyId") Long partyId, @Param("status") String status, Pageable pageable);
    
    /**
     * Find active negotiations between parties
     */
    List<Negotiation> findByBuyerPartyIdAndSellerPartyIdAndStatusOrderByUpdatedAtDesc(
            Long buyerId, Long sellerId, String status);
    
    /**
     * Find negotiations needing attention (ACTIVE)
     */
    List<Negotiation> findByStatusOrderByUpdatedAtAsc(String status);
    
    /**
     * Check if negotiation exists for bid
     */
    Optional<Negotiation> findByBidId(Long bidId);
    
    /**
     * Negotiations by commodity
     */
    List<Negotiation> findByCommodityIdAndStatusOrderByCreatedAtDesc(Long commodityId, String status);
    
    /**
     * Find all negotiations where party is buyer
     */
    List<Negotiation> findByBuyerPartyIdAndStatus(Long buyerPartyId, String status);
    
    /**
     * Find all negotiations where party is seller
     */
    List<Negotiation> findBySellerPartyIdAndStatus(Long sellerPartyId, String status);
}

package com.kalibyte.tradex.trading.repository;

import com.kalibyte.tradex.trading.entity.Demand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DemandRepository extends JpaRepository<Demand, Long> {
    
    Optional<Demand> findByDemandNumber(String demandNumber);
    
    /**
     * Find buyer demands by commodity and status
     */
    List<Demand> findByCommodityIdAndStatusOrderByCreatedAtDesc(Long commodityId, String status);
    
    /**
     * Paginated buyer demands
     */
    Page<Demand> findByCommodityIdAndStatusOrderByCreatedAtDesc(Long commodityId, String status, Pageable pageable);
    
    /**
     * Find party's open demands
     */
    List<Demand> findByPartyIdAndStatusOrderByValidTillAsc(Long partyId, String status);
    
    /**
     * Find demands expiring soon
     */
    List<Demand> findByValidTillBetweenAndStatusOrderByValidTillAsc(LocalDate start, LocalDate end, String status);
    
    /**
     * Check if demand is already fulfilled
     */
    Optional<Demand> findByIdAndStatus(Long id, String status);
    
    /**
     * Active demands in a location zone
     */
    @Query("""
        SELECT d FROM Demand d 
        WHERE d.commodity.id = :commodityId 
        AND d.status = 'OPEN'
        ORDER BY d.targetRate DESC, d.validTill ASC
        """)
    List<Demand> findActiveDemandsByCommodity(@Param("commodityId") Long commodityId);
}

package com.kalibyte.tradex.commodity.repository;

import com.kalibyte.tradex.commodity.entity.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommodityRepository extends JpaRepository<Commodity, Long> {
    Optional<Commodity> findByCommodityCode(String commodityCode);
    
    Optional<Commodity> findByCommodityName(String commodityName);
    
    java.util.List<Commodity> findByParentName(String parentName);

    Optional<Commodity> findByParentNameAndQualityName(String parentName, String qualityName);

    boolean existsByCommodityCode(String commodityCode);
    
    boolean existsByCommodityName(String commodityName);

    boolean existsByParentNameAndQualityName(String parentName, String qualityName);
    
    @Query("SELECT COUNT(c) FROM Commodity c")
    long countAllCommodities();
}

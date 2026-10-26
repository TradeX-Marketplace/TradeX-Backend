package com.kalibyte.tradex.commodity.repository;

import com.kalibyte.tradex.commodity.entity.QualityGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QualityGradeRepository extends JpaRepository<QualityGrade, Long> {
    Optional<QualityGrade> findByCommodityIdAndGradeCode(Long commodityId, String gradeCode);
    
    boolean existsByCommodityIdAndGradeCode(Long commodityId, String gradeCode);
}

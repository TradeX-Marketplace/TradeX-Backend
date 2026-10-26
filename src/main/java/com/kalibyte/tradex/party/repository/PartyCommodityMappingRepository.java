package com.kalibyte.tradex.party.repository;

import com.kalibyte.tradex.party.entity.PartyCommodityMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyCommodityMappingRepository extends JpaRepository<PartyCommodityMapping, Long> {

    List<PartyCommodityMapping> findByPartyId(Long partyId);

    List<PartyCommodityMapping> findByPartyIdAndStatus(Long partyId, String status);

    List<PartyCommodityMapping> findByCommodityIdAndStatus(Long commodityId, String status);

    Optional<PartyCommodityMapping> findByPartyIdAndCommodityIdAndCapabilityType(Long partyId, Long commodityId, String capabilityType);

    void deleteByPartyId(Long partyId);
}

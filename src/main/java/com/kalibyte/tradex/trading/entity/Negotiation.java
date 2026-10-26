package com.kalibyte.tradex.trading.entity;

import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.party.entity.Party;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Negotiation Entity
 * 
 * Represents a multi-round price negotiation between buyer and seller
 * Triggered by a bid or demand-supply match
 */
@Entity
@Table(name = "negotiations", indexes = {
    @Index(name = "idx_negotiations_parties", columnList = "buyer_party_id, seller_party_id"),
    @Index(name = "idx_negotiations_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"bid", "commodity", "buyerParty", "sellerParty", "rounds"})
@EqualsAndHashCode(exclude = {"bid", "commodity", "buyerParty", "sellerParty", "rounds"})
public class Negotiation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String negotiationCode;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    private Bid bid;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_party_id", nullable = false)
    private Party buyerParty;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_party_id", nullable = false)
    private Party sellerParty;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal quantity;
    
    @Column(length = 255)
    private String destination;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal marketRate;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal buyerOffer;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal sellerOffer;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal currentOffer;
    
    @Column(length = 20)
    private String lastOfferBy; // BUYER, SELLER, or ADMIN
    
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
    
    @OneToMany(mappedBy = "negotiation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<NegotiationRound> rounds = new ArrayList<>();
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (negotiationCode == null) {
            negotiationCode = "NEG-" + System.currentTimeMillis();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

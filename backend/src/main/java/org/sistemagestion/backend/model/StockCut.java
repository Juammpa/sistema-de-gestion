package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.util.UUID;

// Una unidad de corte sacada de una pieza de compra.
// La pieza no es una entidad (se calcula como StockService): se identifica por (compra, numero de pieza)
@Entity
@Table(name = "stock_cuts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StockCut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño del registro
    @TenantId
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    // 1, 2, 3, ... (coincide con pieceNumber de PurchasePieceWeight)
    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cut_meat_id", nullable = false)
    private CutMeat cutMeat;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kilograms;

}

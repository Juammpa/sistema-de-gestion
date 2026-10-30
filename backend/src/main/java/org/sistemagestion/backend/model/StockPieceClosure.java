package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

// Pieza de una compra cerrada como vendida: lo que no se corto se toma como merma
// La pieza no es una entidad (se calcula en StockService): se identifica por (compra, numero de pieza)
@Entity
@Table(name = "stock_piece_closures")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StockPieceClosure {

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

    // 1, 2, 3, ... (coincide con pieceNumber de StockCut)
    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

}

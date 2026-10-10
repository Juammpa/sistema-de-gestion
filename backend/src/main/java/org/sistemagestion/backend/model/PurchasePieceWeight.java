package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// Peso ajustado a mano de una pieza de stock.
// Solo existe para las compras cuyo reparto dejo de ser parejo.
// Sin filas aca, la compra se reparte en partes (kg) iguales.
@Entity
@Table(
        name = "purchase_piece_weights",
        uniqueConstraints = @UniqueConstraint(columnNames = {"purchase_id", "piece_number"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PurchasePieceWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "purchase_id", nullable = false)
    private Purchase purchase;

    // 1, 2, 3, ... (coincide con unitNumber de StockItemDto)
    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal kilograms;
}

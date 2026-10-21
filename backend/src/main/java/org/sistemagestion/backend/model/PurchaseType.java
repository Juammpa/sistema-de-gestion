package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import org.sistemagestion.backend.model.enums.Specie;

import java.util.UUID;

@Entity
@Table(name = "purchase_types")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PurchaseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño del registro
    @TenantId
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    private Specie specie;

    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

}

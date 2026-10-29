package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "custom_payment_methods")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CustomPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño del registro
    @TenantId
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID owner_id;

    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}

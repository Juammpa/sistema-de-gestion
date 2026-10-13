package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;

import java.util.UUID;

@Entity
@Table(name = "butcher_shops")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ButcherShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño del registro
    @TenantId
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    private String name;
    private String address;

    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;
}

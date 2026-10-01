package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.sistemagestion.backend.model.enums.SupplierType;

@Entity
@Table(name = "suppliers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierType supplierType;

    @Builder.Default
    private Boolean isActive = true;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;
}

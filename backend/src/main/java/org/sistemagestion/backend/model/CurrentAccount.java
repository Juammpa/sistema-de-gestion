package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "current_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CurrentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_balance", precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @OneToMany(mappedBy = "currentAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movement> movements = new ArrayList<>();
}

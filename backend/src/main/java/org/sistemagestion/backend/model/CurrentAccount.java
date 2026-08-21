package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "current_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class CurrentAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double currentBalance;

    @OneToMany(mappedBy = "currentAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movement> movements = new ArrayList<>();
}

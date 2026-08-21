package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "movements")
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Double amount;
    private Double balance;

    @ManyToOne
    @JoinColumn(name = "current_account_id", nullable = false)
    private CurrentAccount currentAccount;
}

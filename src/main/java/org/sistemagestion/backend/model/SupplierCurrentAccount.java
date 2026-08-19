package org.sistemagestion.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupplierCurrentAccount extends CurrentAccount{

    @OneToOne(mappedBy = "currentAccount")
    private Supplier supplier;
}

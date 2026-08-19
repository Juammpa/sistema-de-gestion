package org.sistemagestion.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "butcher_shop_accounts")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ButcherShopCurrentAccount {

    @OneToOne(mappedBy = "currentAccount")
    private ButcherShop butcherShop;
}

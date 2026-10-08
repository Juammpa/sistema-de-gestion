package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "client")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String lastName;
    private String phoneNumber;

    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ButcherShop> butcherShops = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();

    // Sucusales que no fueron eliminadas
    public List<ButcherShop>  getActiveButcherShops() {
        return butcherShops.stream()
                .filter(shop -> Boolean.TRUE.equals(shop.getIsActive()))
                .toList();
    }

}

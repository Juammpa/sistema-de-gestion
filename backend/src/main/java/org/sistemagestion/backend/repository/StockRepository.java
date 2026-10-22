package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.dto.stock.StockPurchaseDto;
import org.sistemagestion.backend.model.Purchase;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockRepository extends Repository<Purchase, Long> {

    // Compras de todos los proveedores en el rango, de la mas reciente a la mas antigua
    @Query("""
            select new org.sistemagestion.backend.dto.stock.StockPurchaseDto(
                p.id, p.date, pt.id, pt.name, pt.specie, p.unit, p.kilograms, s.id, s.companyName)
            from Purchase p
            join p.purchaseType pt
            join Supplier s on s.currentAccount = p.currentAccount
            where p.date between :from and :to
            order by p.date desc, p.id desc
            """)
    List<StockPurchaseDto> findPurchases(@Param("from") LocalDate from, @Param("to") LocalDate to);

}

package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.dto.dashboard.DebtRankingDto;
import org.sistemagestion.backend.dto.dashboard.MonthlyTotalDto;
import org.sistemagestion.backend.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DashboardRepository extends Repository<Payment, Long> {

    // ==== Totales por mes ====
    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.MonthlyTotalDto(
                year(s.date), month(s.date), sum(s.amount))
            from Sale s
            where s.date between :from and :to
            group by year(s.date), month(s.date)
            """)
    List<MonthlyTotalDto> salesByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.MonthlyTotalDto(
                year(p.date), month(p.date), sum(p.amount))
            from Purchase p
            where p.date between :from and :to
            group by year(p.date), month(p.date)
            """)
    List<MonthlyTotalDto> purchasesByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Pagos de cuentas de clientes (cobros)
    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.MonthlyTotalDto(
                year(p.date), month(p.date), sum(p.amount))
            from Payment p
            where p.date between :from and :to
              and exists (select 1 from ButcherShop bs where bs.currentAccount = p.currentAccount)
            group by year(p.date), month(p.date)
            """)
    List<MonthlyTotalDto> collectionsByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Pagos de cuentas de proveedores
    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.MonthlyTotalDto(
                year(p.date), month(p.date), sum(p.amount))
            from Payment p
            where p.date between :from and :to
              and exists (select 1 from Supplier sup where sup.currentAccount = p.currentAccount)
            group by year(p.date), month(p.date)
            """)
    List<MonthlyTotalDto> supplierPaymentsByMonth(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ==== Saldos ====

    @Query("select sum(a.currentBalance) from ButcherShop bs join bs.currentAccount a")
    Double totalClientBalance();

    @Query("select sum(a.currentBalance) from Supplier s join s.currentAccount a")
    Double totalSupplierBalance();

    // ==== Rankings ====

    // Clientes con mas deuda (se suman sus sucursales)
    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.DebtRankingDto(
                c.id, concat(c.lastName, ' ', c.name), sum(a.currentBalance))
            from ButcherShop bs
            join bs.client c
            join bs.currentAccount a
            group by c.id, c.lastName, c.name
            having sum(a.currentBalance) > 0
            order by sum(a.currentBalance) desc
            """)
    List<DebtRankingDto> topClientDebts(Pageable pageable);

    // Proveedores a los que mas se les debe
    @Query("""
            select new org.sistemagestion.backend.dto.dashboard.DebtRankingDto(
                s.id, s.companyName, a.currentBalance)
            from Supplier s
            join s.currentAccount a
            where a.currentBalance > 0
            order by a.currentBalance desc
            """)
    List<DebtRankingDto> topSupplierDebts(Pageable pageable);
}

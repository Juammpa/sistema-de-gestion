package org.sistemagestion.backend.repository;

import org.sistemagestion.backend.dto.cash.CashPaymentDto;
import org.sistemagestion.backend.dto.cash.CashWithdrawalDto;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CashRepository extends Repository<Payment, Long> {

    // Pagos recibidos de clientes en el rango
    @Query("""
            select new org.sistemagestion.backend.dto.cash.CashPaymentDto(
                p.id, p.date, p.paymentType, p.amount,
                concat(c.lastName, ' ', c.name), bs.name, s.companyName, cpm.name)
            from Payment p
            join ButcherShop bs on bs.currentAccount = p.currentAccount
            join bs.client c
            left join p.supplier s
            left join p.customPaymentMethod cpm
            where p.date between :from and :to
            order by p.date, p.id
            """)
    List<CashPaymentDto> findClientPayments(@Param("from")LocalDate from, @Param("to") LocalDate to);

    // Pagos hechos a proveedores en el rango (salidas de caja), sin los pagos espejos
    @Query("""
            select new org.sistemagestion.backend.dto.cash.CashWithdrawalDto(
                p.id, p.date, p.amount, sup.companyName)
            from Payment p
            join Supplier sup on sup.currentAccount = p.currentAccount
            where p.originPayment is null and p.date between :from and :to
            order by p.date, p.id
            """)
    List<CashWithdrawalDto> findSupplierPayments(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // Saldo de arrastre: todoo lo cobrado a clientes antes de una fecha (menos transf. a proveedor)
    @Query("""
            select sum(case when p.paymentType = org.sistemagestion.backend.model.enums.PaymentType.CHECK_REJECTION
                                                   then -p.amount else p.amount end)
            from Payment p
            where p.paymentType <> :excluded and p.date < :date
              and exists (select 1 from ButcherShop bs where bs.currentAccount = p.currentAccount)
            """)
    BigDecimal sumClientPaymentsBefore(@Param("excluded") PaymentType excluded, @Param("date") LocalDate date);

    // Saldo de arrastre: lo pagado a proveedores antes de una fecha (sin pagos espejos)
    @Query("""
            select sum(p.amount)
            from Payment p
            where p.originPayment is null and p.date < :date
              and exists (select 1 from Supplier sup where sup.currentAccount = p.currentAccount)
            """)
    BigDecimal sumSupplierPaymentsBefore(@Param("date") LocalDate date);

    // Total historico que entro a la caja por cobros a clientes (todoo menos SUPPLIER_TRANSFER)
    @Query("""
        select sum(case when p.paymentType = org.sistemagestion.backend.model.enums.PaymentType.CHECK_REJECTION
                        then -p.amount else p.amount end)
        from Payment p
        where p.paymentType <> :excluded
          and exists (select 1 from ButcherShop bs where bs.currentAccount = p.currentAccount)
        """)
    BigDecimal sumClientPayments(@Param("excluded") PaymentType excluded);

    // Total historico pagado a proveedores (sin pagos espejos)
    @Query("""
        select sum(p.amount)
        from Payment p
        where p.originPayment is null
          and exists (select 1 from Supplier sup where sup.currentAccount = p.currentAccount)
        """)
    BigDecimal sumSupplierPayments();
}

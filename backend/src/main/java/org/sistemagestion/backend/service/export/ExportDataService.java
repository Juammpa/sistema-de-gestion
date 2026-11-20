package org.sistemagestion.backend.service.export;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.repository.MovementRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

// Todos los metodos se llaman dentro de la transaccion de solo lectura de ExportService:
// Payment.supplier y Payment.originPayment son LAZY
@Service
@RequiredArgsConstructor
public class ExportDataService {

    // Limite de Excel para el nombre de una hoja
    private static final int MAX_SHEET_NAME = 31;

    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;
    private final MovementRepository movementRepository;
    private final ButcherShopRepository butcherShopRepository;
    private final EntityManager entityManager;

    // ---------- Plan --------------

    // Que se exporta, sin movimientos: solo nombres, cuentas y saldos
    public ExportPlan plan() {

        List<Client> clients = clientRepository.findActiveWithButcherShops();
        List<Supplier> suppliers = supplierRepository.findByIsActiveTrueOrderByCompanyNameAsc();

        // "resumen" ya lo usa la hoja de resumen del archivo de clientes.
        Set<String> usedSheetNames = new HashSet<>(Set.of("resumen"));
        Map<Long, String> clientNameByAccount = new HashMap<>();

        List<ExportPlan.ClientPlan> clientPlans = new ArrayList<>();
        for (Client client: clients) {

            String clientName = fullName(client);
            List<ExportPlan.ShopPlan> shops = new ArrayList<>();

            for (ButcherShop shop: client.getActiveButcherShops()) {
                CurrentAccount account = shop.getCurrentAccount();
                shops.add(new ExportPlan.ShopPlan(shop.getName(), shop.getAddress(),account.getId(), balanceOf(account)));
                clientNameByAccount.put(account.getId(), clientName);
            }

            clientPlans.add(new ExportPlan.ClientPlan(uniqueSheetName(clientName, usedSheetNames), clientName, shops));
        }

        List<ExportPlan.SupplierPlan> supplierPlans = new ArrayList<>();
        for (Supplier supplier: suppliers) {
            CurrentAccount account = supplier.getCurrentAccount();
            supplierPlans.add(new ExportPlan.SupplierPlan(
                    supplier.getCompanyName(), supplier.getSupplierType(), account.getId(), balanceOf(account)));
        }

        return new ExportPlan(clientPlans, supplierPlans, clientNameByAccount);
    }



    // ---------- Clientes --------------

    // Carga los movimientos de las sucursales de un cliente y los convierte en filas
    public ExportData.ClientSheet loadClientSheet(ExportPlan.ClientPlan client) {

        boolean severalShops = client.shops().size() > 1;

        List<ExportData.ShopBlock> blocks = new ArrayList<>();

        for (ExportPlan.ShopPlan shop: client.shops()) {
            List<ExportData.ClientRow> rows = movementRepository
                    .findByCurrentAccountIdOrderByDateAscIdAsc(shop.accountId()).stream()
                    .map(this::toClientRow)
                    .toList();

            // Si tiene una sucursal, va solo el nombre del cliente
            String title = severalShops ? client.fullName() + " (" + shopLabel(shop) + ")" : client.fullName();
            blocks.add(new ExportData.ShopBlock(title, shop.balance(), rows));
        }

        // Las entidades ya estan convertidas a filas: se sueltan para que no se acumulen en la sesion
        entityManager.clear();

        return new ExportData.ClientSheet(client.sheetName(), blocks);
    }

    // "Centro - Calle 123"; si la sucursal no tiene direccion, solo el nombre
    private String shopLabel(ExportPlan.ShopPlan shop) {

        String address = shop.address() == null ? "" : shop.address().trim();

        return address.isEmpty() ? shop.name() : shop.name() + " - " + address;
    }

    private ExportData.ClientRow toClientRow(Movement movement) {
        return switch (movement) {

            case Sale sale -> new ExportData.ClientRow(
                    sale.getDate(), "Venta", sale.getCutMeatName(),
                    sale.getUnit(), sale.getKilograms(), sale.getPricePerKilo(),
                    null, sale.getAmount(), sale.getBalance(), false);

            // El rechazo de un cheque vuelve a sumar a la deuda: va en "Monto total", como una venta
            case Payment payment when payment.getPaymentType() == PaymentType.CHECK_REJECTION -> new ExportData.ClientRow(
                    payment.getDate(),
                    ExportLabels.paymentType(payment),
                    null, null, null, null,
                    null, payment.getAmount(),
                    payment.getBalance(), false);

            case Payment payment -> new ExportData.ClientRow(
                    payment.getDate(), clientPaymentLabel(payment),
                    null, null, null, null,
                    payment.getAmount(), null, payment.getBalance(),
                    payment.getPaymentType() == PaymentType.SUPPLIER_TRANSFER);

            default -> throw new IllegalStateException(
                    "Movimiento inesperado en la cuenta de un cliente: " +
                            movement.getClass().getSimpleName());
        };
    }

    // "Transferencia Meyer" cuando el cliente le pago directamente a un proveedor
    private String clientPaymentLabel(Payment payment) {

        if (payment.getPaymentType() == PaymentType.SUPPLIER_TRANSFER && payment.getSupplier() != null) {
            return "Transferencia " + payment.getSupplier().getCompanyName();
        }

        return ExportLabels.paymentType(payment);
    }



    // ---------- Proveedores --------------

    // Carga los movimientos de la cuenta de un proveedor y los convierte en filas
    public ExportData.SupplierBook loadSupplierBook(ExportPlan plan, ExportPlan.SupplierPlan supplier) {

        List<ExportData.SupplierRow> rows = movementRepository
                .findByCurrentAccountIdOrderByDateAscIdAsc(supplier.accountId()).stream()
                .map(movement -> toSupplierRow(movement, plan))
                .toList();

        entityManager.clear();

        return new ExportData.SupplierBook(supplier.companyName(), supplier.supplierType(), supplier.balance(), rows);
    }

    private ExportData.SupplierRow toSupplierRow(Movement movement, ExportPlan plan) {
        return switch (movement) {

            case Purchase purchase -> new ExportData.SupplierRow(
                    purchase.getDate(), purchase.getPurchaseType().getName(),
                    purchase.getUnit(), purchase.getKilograms(), purchase.getPricePerKilo(),
                    purchase.getAmount(), null, purchase.getBalance(), false);

            case Payment payment -> new ExportData.SupplierRow(
                    payment.getDate(), supplierPaymentLabel(payment, plan),
                    null, null, null, null,
                    payment.getAmount(), payment.getBalance(),
                    payment.getOriginPayment() != null);

            default -> throw new IllegalStateException(
                    "Movimiento inesperado en la cuenta de un proveedor: " +
                            movement.getClass().getSimpleName());
        };
    }

    // "Transferencia Maspi" cuando es un pago espejo (un cliente le transfirio al proveedor)
    private String supplierPaymentLabel(Payment payment, ExportPlan plan) {

        Payment origin = payment.getOriginPayment();

        if (origin == null) {
            return ExportLabels.paymentType(payment);
        }

        String clientName = clientNameOf(plan, origin.getCurrentAccount().getId());

        return clientName != null ? "Transferencia " + clientName : "Tansferencia";
    }

    // Nombre del cliente dueno de una cuenta. Los clientes activos ya estan en el plan;
    // si fue eliminado (borrado logico) se busca una vez y se recuerda para los demas pagos
    private String clientNameOf(ExportPlan plan, Long accountId) {

        String name = plan.clientNameByAccount().get(accountId);
        if (name != null) return name;

        name = butcherShopRepository.findByCurrentAccountId(accountId)
                .map(ButcherShop::getClient)
                .map(this::fullName)
                .orElse(null);

        if (name != null) {
            plan.clientNameByAccount().put(accountId, name);
        }

        return name;
    }



    // ---------- Utilidades --------------

    // "Apellido Nombre" del cliente
    private String fullName(Client client) {
        return String.format("%s %s", client.getLastName(), client.getName().trim());
    }

    private BigDecimal balanceOf(CurrentAccount account) {
        return Decimals.moneyOrZero(account.getCurrentBalance());
    }

    // Excel no admite mas de 31 caracteres, ni : \ / ?  * [ ], ni nombres repetidos
    private String uniqueSheetName(String wanted, Set<String> used) {

        String base = wanted.replaceAll("[\\\\/:?*\\[\\]]", " ")
                .replaceAll("^'+|'+$", "")
                .trim();

        if (base.isEmpty()) {
            base = "Cliente";
        }

        base = truncate(base, MAX_SHEET_NAME);

        String candidate = base;
        int n = 2;
        while (!used.add(candidate.toLowerCase(Locale.ROOT))) {
            String suffix = " (" + n++ + ")";
            candidate = truncate(base, MAX_SHEET_NAME - suffix.length()) + suffix;
        }

        return candidate;
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max).trim();
    }

}
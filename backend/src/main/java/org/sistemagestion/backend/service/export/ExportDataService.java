package org.sistemagestion.backend.service.export;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.repository.MovementRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportDataService {

    // Limite de Excel para el nombre de una hoja
    private static final int MAX_SHEET_NAME = 31;

    private final ClientRepository clientRepository;
    private final SupplierRepository supplierRepository;
    private final MovementRepository movementRepository;
    private final ButcherShopRepository butcherShopRepository;

    // Todoo en una transaccion: Payment.supplier y Payment.originPayment son LAZY
    @Transactional(readOnly = true)
    public ExportData load() {

        List<Client> clients = clientRepository.findActiveWithButcherShops();
        List<Supplier> suppliers = supplierRepository.findByIsActiveTrueOrderByCompanyNameAsc();

        Map<Long, List<Movement>> movementsByAccount = loadMovements(clients, suppliers);
        Map<Long, Client> clientByAccount = clientsByAccountId(clients);

        // "resumen" ya lo usa la hoja de resumen del archivo de clientes.
        Set<String> usedSheetNames = new HashSet<>(Set.of("resumen"));

        List<ExportData.ClientSheet> clientSheets = new ArrayList<>();
        for (Client client: clients) {
            clientSheets.add(toClientSheet(client, movementsByAccount, usedSheetNames));
        }

        List<ExportData.SupplierBook> supplierBooks = new ArrayList<>();
        for (Supplier supplier: suppliers) {
            supplierBooks.add(toSupplierBook(supplier, movementsByAccount, clientByAccount));
        }

        return new ExportData(clientSheets, supplierBooks);
    }



    // ---------- Lectura --------------

    // Una sola consulta para todas las cuentas (sucursales y proveedores), agrupadas por cuenta
    private Map<Long, List<Movement>> loadMovements(List<Client> clients, List<Supplier> suppliers) {

        List<Long> accountIds = new ArrayList<>();

        for (Client client: clients) {
            for (ButcherShop shop: client.getActiveButcherShops()) {
                accountIds.add(shop.getCurrentAccount().getId());
            }
        }

        for (Supplier supplier: suppliers) {
            accountIds.add(supplier.getCurrentAccount().getId());
        }

        if (accountIds.isEmpty()) {
            return Map.of();
        }

        // groupingBy conserva el orden de la consulta (fecha e id ascendente) dentro de cada cuenta
        return movementRepository.findByCurrentAccountIdInOrderByDateAscIdAsc(accountIds).stream()
                .collect(Collectors.groupingBy(m -> m.getCurrentAccount().getId()));
    }

    // Cuenta corriente de una sucursal --> Su cliente (para nombras los pagos espejo)
    private Map<Long, Client> clientsByAccountId(List<Client> clients) {

        Map<Long, Client> byAccount = new HashMap<>();
        for (Client client: clients) {
            for (ButcherShop shop: client.getActiveButcherShops()) {
                byAccount.put(shop.getCurrentAccount().getId(), client);
            }
        }

        return byAccount;
    }



    // ---------- Clientes --------------

    private ExportData.ClientSheet toClientSheet(Client client, Map<Long, List<Movement>> movementsByAccount, Set<String> usedSheetNames) {

        String clientName = fullName(client);
        boolean severalShops = client.getActiveButcherShops().size() > 1;

        List<ExportData.ShopBlock> blocks = new ArrayList<>();

        for (ButcherShop shop: client.getActiveButcherShops()) {
            CurrentAccount account = shop.getCurrentAccount();
            List<ExportData.ClientRow> rows =
                    movementsByAccount.getOrDefault(account.getId(), List.of()).stream()
                            .map(this::toClientRow)
                            .toList();

            // Si tiene una sucursal, va solo el nombre del cliente
            String title = severalShops ? clientName + " (" + shop.getName() + ")" : clientName;
            blocks.add(new ExportData.ShopBlock(title, balanceOf(account), rows));
        }

        return new ExportData.ClientSheet(uniqueSheetName(clientName, usedSheetNames), blocks);
    }

    private ExportData.ClientRow toClientRow(Movement movement) {
        return switch (movement) {

            case Sale sale -> new ExportData.ClientRow(
                    sale.getDate(), "Venta", sale.getCutMeatName(),
                    sale.getUnit(), sale.getKilograms(), sale.getPricePerKilo(),
                    null, sale.getAmount(), sale.getBalance(), false);

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

        return ExportLabels.paymentType(payment.getPaymentType());
    }



    // ---------- Proveedores --------------

    private ExportData.SupplierBook toSupplierBook(Supplier supplier, Map<Long, List<Movement>> movementsByAccount, Map<Long, Client> clientByAccount) {

        CurrentAccount account = supplier.getCurrentAccount();
        List<ExportData.SupplierRow> rows = movementsByAccount.getOrDefault(account.getId(), List.of())
                .stream().map(movement -> toSupplierRow(movement,clientByAccount))
                .toList();

        return new ExportData.SupplierBook(supplier.getCompanyName(), supplier.getSupplierType(), balanceOf(account), rows);
    }

    private ExportData.SupplierRow toSupplierRow(Movement movement, Map<Long, Client> clientByAccount) {
        return switch (movement) {

            case Purchase purchase -> new ExportData.SupplierRow(
                    purchase.getDate(), ExportLabels.purchaseType(purchase.getPurchaseType()),
                    purchase.getUnit(), purchase.getKilograms(), purchase.getPricePerKilo(),
                    purchase.getAmount(), null, purchase.getBalance(), false);

            case Payment payment -> new ExportData.SupplierRow(
                    payment.getDate(), supplierPaymentLabel(payment, clientByAccount),
                    null, null, null, null,
                    payment.getAmount(), payment.getBalance(),
                    payment.getOriginPayment() != null);

            default -> throw new IllegalStateException(
                    "Movimiento inesperado en la cuenta de un proveedor: " +
                    movement.getClass().getSimpleName());
        };
    }

    // "Transferencia Maspi" cuando es un pago espejo (un cliente le transfirio al proveedor)
    private String supplierPaymentLabel(Payment payment, Map<Long, Client> clientByAccount) {

        Payment origin = payment.getOriginPayment();

        if (origin == null) {
            return ExportLabels.paymentType(payment.getPaymentType());
        }

        Long originAccountId = origin.getCurrentAccount().getId();
        Client client = clientByAccount.get(originAccountId);

        if(client == null) {
            // El cliente fue eliminado (borrado logico), asi que no esta entre los activos
            client = butcherShopRepository.findByCurrentAccountId(originAccountId)
                    .map(ButcherShop::getClient)
                    .orElse(null);
        }

        return client != null ? "Transferencia " + fullName(client) : "Tansferencia";
    }



    // ---------- Utilidades --------------

    // "Apellido Nombre" del cliente
    private String fullName(Client client) {
        return String.format("%s %s", client.getLastName(), client.getName().trim());
    }

    private double balanceOf(CurrentAccount account) {
        return Objects.requireNonNullElse(account.getCurrentBalance(), 0.0);
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

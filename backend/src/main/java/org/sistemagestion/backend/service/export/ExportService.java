package org.sistemagestion.backend.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportService implements IExportService {

    private static final String CLIENTS_FILE = "Clientes.xlsx";

    private final ExportDataService exportDataService;

    // Una sola transaccion de solo lectura para ttodo el export (hay relaciones LAZY).
    // Los movimientos se cargan de a un cliente o proveedor, y cada Excel se escribe en el ZIP a medida que se arma
    @Override
    @Transactional(readOnly = true)
    public void exportAll(OutputStream out) throws IOException {

        ExportPlan plan = exportDataService.plan();
        ZipOutputStream zip = new ZipOutputStream(out);

        zip.putNextEntry(new ZipEntry(CLIENTS_FILE));
        ClientsWorkbookBuilder.write(clientSheets(plan), zip);
        zip.closeEntry();

        Set<String> usedNames = new HashSet<>();
        for (ExportPlan.SupplierPlan supplier: plan.suppliers()) {
            zip.putNextEntry(new ZipEntry(uniqueFileName(supplier.companyName(), usedNames)));
            SupplierWorkbookBuilder.write(exportDataService.loadSupplierBook(plan, supplier), zip);
            zip.closeEntry();
        }

        // Cierra el ZIP sin cerrar la respuesta: eso lo hace el servidor
        zip.finish();
    }

    // Las hojas de clientes se cargan una por una, justo cuando el libro las pide
    private Iterator<ExportData.ClientSheet> clientSheets(ExportPlan plan) {

        Iterator<ExportPlan.ClientPlan> clients = plan.clients().iterator();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return clients.hasNext();
            }

            @Override
            public ExportData.ClientSheet next() {
                return exportDataService.loadClientSheet(clients.next());
            }
        };
    }

    // "Cuenta_Cte_Meyer.xlsx"; si dos proveedores se llaman igual, el segundo lleva _2
    private String uniqueFileName(String companyName, Set<String> used) {

        String base = "Cuenta_Cte_" + safeName(companyName);
        String name = base + ".xlsx";

        int n = 2;
        while (!used.add(name.toLowerCase(Locale.ROOT))) {
            name = base + "_" + n++ + ".xlsx";
        }

        return name;
    }

    // Sin tildes ni simbolos: "Figorífico Del Sur S.A." --> "Frigorifico_Del_Sur_S_A"
    private String safeName(String text) {
        String plain = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String cleaned = plain.trim().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_+|_+$", "");

        return cleaned.isEmpty() ? "Proveedor" : cleaned;
    }

}
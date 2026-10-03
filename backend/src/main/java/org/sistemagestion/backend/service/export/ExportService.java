package org.sistemagestion.backend.service.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ExportService implements IExportService {

    private static final String CLIENTS_FILE = "Clientes.xlsx";

    private final ExportDataService exportDataService;

    @Override
    public ExportFile exportAll() {
        ExportData data = exportDataService.load();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {

            addEntry(zip, CLIENTS_FILE, ClientsWorkbookBuilder.build(data.clients()));

            Set<String> usedNames = new HashSet<>();
            for (ExportData.SupplierBook supplier: data.suppliers()) {
                addEntry(zip, uniqueFileName(supplier.companyName(), usedNames), SupplierWorkbookBuilder.build(supplier));
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // El ZIP se cierra al salir del try: recien ahi los bytes estan completos
        return new ExportFile("exportacion_" + LocalDate.now() + ".zip", bytes.toByteArray());
    }

    private void addEntry(ZipOutputStream zip, String name, byte[] content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content);
        zip.closeEntry();
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


package org.sistemagestion.backend.service.export;

import java.io.IOException;
import java.io.OutputStream;

public interface IExportService {

    // Escribe el ZIP (Excel de clientes + un Excel por proveedor) directo en out, a medida que lo arma
    void exportAll(OutputStream out) throws IOException;

}

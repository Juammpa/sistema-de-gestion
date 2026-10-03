package org.sistemagestion.backend.service.export;

public interface IExportService {

    // ZIP con el Excel de clientes y un Excel por proveedor
    ExportFile exportAll();

}

package org.sistemagestion.backend.controller;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.service.export.ExportFile;
import org.sistemagestion.backend.service.export.IExportService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final IExportService exportService;

    // El ZIP con el Excel de clientes y un Excel por proveedor
    @GetMapping
    public ResponseEntity<byte[]> export() {

        ExportFile file = exportService.exportAll();
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                            .contentType(MediaType.parseMediaType("application/zip"))
                            .contentLength(file.content().length)
                            .cacheControl(CacheControl.noStore())
                            .body(file.content());

    }

}

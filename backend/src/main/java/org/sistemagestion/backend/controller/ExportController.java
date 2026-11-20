package org.sistemagestion.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.service.export.IExportService;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final IExportService exportService;

    // El ZIP con el Excel de clientes y un Excel por proveedor.
    // Se escribe directo en la respuesta, a medida que se arma: no se conoce el tamano de antemano,
    // asi que la respuesta no lleva Content-Length
    @GetMapping
    public void export(HttpServletResponse response) throws IOException {

        String fileName = "exportacion_" + LocalDate.now() + ".zip";

        response.setContentType("application/zip");
        response.setHeader(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(fileName).build().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue());

        exportService.exportAll(response.getOutputStream());
    }

}
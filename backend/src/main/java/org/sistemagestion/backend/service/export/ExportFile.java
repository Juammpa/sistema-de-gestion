package org.sistemagestion.backend.service.export;

// Archivo listo para descargar
public record ExportFile(
        String fileName,
        byte[] content
) { }

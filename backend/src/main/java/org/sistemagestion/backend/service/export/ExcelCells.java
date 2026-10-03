package org.sistemagestion.backend.service.export;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

// Helpers para escribir celdas.
// Una celda sin valor igual se crea con su estilo, para que se dibuje el borde
public class ExcelCells {

    private ExcelCells() {}

    // Devuelve la fila (la crea si no existe): Los bloques de varias sucursales comparten filas.
    static Row row(Sheet sheet, int index, int heightPoints) {

        Row row = sheet.getRow(index);

        if (row == null) {
            row = sheet.createRow(index);
        }

        row.setHeightInPoints(heightPoints);
        return row;
    }

    static void text(Row row, int col, String value, CellStyle style) {

        Cell cell = cell(row, col, style);
        if (value !=  null) {
            cell.setCellValue(value);
        }
    }

    static void number(Row row, int col, Number value, CellStyle style) {

        Cell cell = cell(row, col, style);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
    }

    static void date(Row row, int col, LocalDate value, CellStyle style) {

        Cell cell = cell(row, col, style);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    // Texto en celdas combinadas: El estilo va en todas, si no el borde se dibuja incompleto
    static void merged(Sheet sheet, Row row, int firstCol, int lastCol, String value, CellStyle style) {

        for (int col = firstCol; col <= lastCol; col++) {
            cell(row, col, style);
        }

        row.getCell(firstCol).setCellValue(value);
        mergeRegion(sheet, row.getRowNum(), firstCol, lastCol);
    }

    // POI no deja de combinar una sola celda
    static void mergeRegion(Sheet sheet, int rowIndex, int firstCol, int lastCol) {

        if (lastCol > firstCol) {
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, firstCol, lastCol));
        }
    }

    static byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        workbook.write(out);

        return out.toByteArray();
    }

    private static Cell cell(Row row, int col, CellStyle style) {

        Cell cell = row.createCell(col);
        cell.setCellStyle(style);

        return cell;
    }


}


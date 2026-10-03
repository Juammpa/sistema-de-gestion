package org.sistemagestion.backend.service.export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sistemagestion.backend.service.export.ExcelStyles.CellKind;
import org.sistemagestion.backend.service.export.ExcelStyles.Look;

import java.io.IOException;
import java.io.UncheckedIOException;

// Archivo de un proveedor: una sola hoja con su cuenta corriente
final class SupplierWorkbookBuilder {

    private static final String SHEET_NAME = "Cuenta Corriente";
    private static final int COLUMNS = 8;

    private static final int TITLE_ROW = 0;
    private static final int HEADER_ROW = 1;
    private static final int FIRST_DATA_ROW = 2;

    private SupplierWorkbookBuilder() {}

    static byte[] build(ExportData.SupplierBook supplier) {
        Look look = Look.forSupplier(supplier.supplierType());

        String[] headers = {"Fecha", "Tipo", "Unidad", "Kilos (Kg)",
        "Precio", "Debe", "Haber", "Saldo"};

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            ExcelStyles styles = new ExcelStyles(workbook);
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            for (int c = 0; c < COLUMNS; c++) {
                sheet.setColumnWidth(c, ExcelStyles.columnWidth(ExcelStyles.SUPPLIER_WIDTHS[c]));
            }

            Row titleRow = ExcelCells.row(sheet, TITLE_ROW, ExcelStyles.TITLE_ROW_HEIGHT);
            ExcelCells.merged(sheet, titleRow, 0, COLUMNS - 1,
                    "Cuenta Corriente " + supplier.companyName(), styles.title(look, 0));

            Row headerRow = ExcelCells.row(sheet, HEADER_ROW, ExcelStyles.HEADER_ROW_HEIGHT);
            for (int c = 0; c < COLUMNS; c++) {
                ExcelCells.text(headerRow, c, headers[c], styles.header(look, c == COLUMNS - 1));
            }

            int rowIndex = FIRST_DATA_ROW;
            for (ExportData.SupplierRow data : supplier.rows()) {
                Row row = ExcelCells.row(sheet, rowIndex++, ExcelStyles.BODY_ROW_HEIGHT);
                boolean h = data.highlighted();

                ExcelCells.date(row, 0, data.date(), styles.body(look, CellKind.DATE, h));
                ExcelCells.text(row, 1, data.type(), styles.body(look, CellKind.TEXT, h));
                ExcelCells.number(row, 2, data.unit(), styles.body(look, CellKind.UNIT, h));
                ExcelCells.number(row, 3, data.kilograms(), styles.body(look, CellKind.KILOS, h));
                ExcelCells.number(row, 4, data.pricePerKilo(), styles.body(look, CellKind.MONEY, h));
                ExcelCells.number(row, 5, data.debit(), styles.body(look, CellKind.MONEY, h));
                ExcelCells.number(row, 6, data.credit(), styles.body(look, CellKind.MONEY, h));
                ExcelCells.number(row, 7, data.balance(), styles.body(look, CellKind.BALANCE, h));
            }

            sheet.createFreezePane(0, FIRST_DATA_ROW);
            return ExcelCells.toBytes(workbook);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
}

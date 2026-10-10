package org.sistemagestion.backend.service.export;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.service.export.ExcelStyles.CellKind;
import org.sistemagestion.backend.service.export.ExcelStyles.Look;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Archivo de clientes: una hoja por cliente + Resumen
final class ClientsWorkbookBuilder {

    private static final String[] HEADERS = {
            "Fecha", "Tipo", "Corte", "Unidad", "Kilos (Kg)",
            "Precio", "Entregado", "Monto Total", "Saldo"
    };
    private static final int COLUMNS = HEADERS.length;

    // Entre una sucursal y la siguiente queda una columna angosta de separacion
    private static final int BLOCK_STRIDE = COLUMNS + 1;
    private static final int GAP_WIDTH = 2;

    private static final int TITLE_ROW = 0;
    private static final int HEADER_ROW = 1;
    private static final int FIRST_DATA_ROW = 2;

    // Resumen: grupos de 3 columnas (N°, Nombre, Saldo) + 1 de separacion, 10 clientes por grupo
    private static final String SUMMARY_SHEET = "Resumen";
    private static final int SUMMARY_GROUP_SIZE = 10;
    private static final int SUMMARY_STRIDE = 4;
    private static final int SUMMARY_ROW_HEIGHT = 25;
    private static final int SUMMARY_BIG_ROW_HEIGHT = 30;

    private ClientsWorkbookBuilder() {}

    static byte[] build(List<ExportData.ClientSheet> clients) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            ExcelStyles styles = new ExcelStyles(workbook);

            List<ExportData.ShopBlock> allBlocks = new ArrayList<>();
            for (ExportData.ClientSheet client: clients) {

                writeClientSheet(workbook.createSheet(client.sheetName()), client, styles);
                allBlocks.addAll(client.blocks());
            }

            writeSummary(workbook.createSheet(SUMMARY_SHEET), allBlocks, styles);

            return ExcelCells.toBytes(workbook);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------- Hoja de un cliente ----------------

    private static void writeClientSheet(Sheet sheet, ExportData.ClientSheet client, ExcelStyles styles) {

        List<ExportData.ShopBlock> blocks = client.blocks();
        for (int i = 0; i < blocks.size(); i++) {
            writeBlock(sheet, blocks.get(i), i, styles);
        }

        // Titulo y encabezados quedan fijos al desplazarse
        sheet.createFreezePane(0, FIRST_DATA_ROW);
    }

    private static void writeBlock(Sheet sheet, ExportData.ShopBlock block, int index, ExcelStyles styles) {

        Look look = Look.CLIENT;
        int first = index * BLOCK_STRIDE;

        for (int c = 0; c < COLUMNS; c++) {
            sheet.setColumnWidth(first + c,
                    ExcelStyles.columnWidth(ExcelStyles.CLIENT_WIDTHS[c]));
        }

        if (index > 0) {
            sheet.setColumnWidth(first -1, ExcelStyles.columnWidth(GAP_WIDTH));
        }

        Row titleRow = ExcelCells.row(sheet, TITLE_ROW, ExcelStyles.TITLE_ROW_HEIGHT);
        ExcelCells.merged(sheet, titleRow, first, first + COLUMNS -1, block.title(), styles.title(look, index));

        Row headerRow = ExcelCells.row(sheet, HEADER_ROW, ExcelStyles.HEADER_ROW_HEIGHT);
        for (int c = 0; c < COLUMNS; c++) {
            ExcelCells.text(headerRow, first + c, HEADERS[c], styles.header(look, c == COLUMNS -1));
        }

        int rowIndex = FIRST_DATA_ROW;
        for (ExportData.ClientRow data: block.rows()) {
            Row row = ExcelCells.row(sheet, rowIndex++, ExcelStyles.BODY_ROW_HEIGHT);
            boolean h = data.highlighted();

            ExcelCells.date(row, first, data.date(), styles.body(look, CellKind.DATE, h));
            ExcelCells.text(row, first + 1, data.type(), styles.body(look, CellKind.TEXT, h));
            ExcelCells.text(row, first + 2, data.cutMeat(), styles.body(look, CellKind.TEXT, h));
            ExcelCells.number(row, first + 3, data.unit(), styles.body(look, CellKind.UNIT, h));
            ExcelCells.number(row, first + 4, data.kilograms(), styles.body(look, CellKind.KILOS, h));
            ExcelCells.number(row, first + 5, data.pricePerKilo(), styles.body(look, CellKind.MONEY, h));
            ExcelCells.number(row, first + 6, data.delivered(), styles.body(look, CellKind.MONEY, h));
            ExcelCells.number(row, first + 7, data.total(), styles.body(look, CellKind.MONEY, h));
            ExcelCells.number(row, first + 8, data.balance(), styles.body(look, CellKind.BALANCE, h));
        }
    }

    // ------------- Resumen ------------------

    private static void writeSummary(Sheet sheet, List<ExportData.ShopBlock> blocks, ExcelStyles styles) {

        int groups = Math.max(1, (blocks.size() + SUMMARY_GROUP_SIZE - 1)/ SUMMARY_GROUP_SIZE);

        // Anchos: N° | Nombre | Saldo Pendiente | separacion
        for (int g = 0; g < groups; g++) {
            int first = g * SUMMARY_STRIDE;
            sheet.setColumnWidth(first, ExcelStyles.columnWidth(7));
            sheet.setColumnWidth(first + 1, ExcelStyles.columnWidth(38));
            sheet.setColumnWidth(first + 2, ExcelStyles.columnWidth(31));
            sheet.setColumnWidth(first + 3, ExcelStyles.columnWidth(4));
        }

        // Saldo total, arriba, sobre las columnas Nombre y Saldo del primer grupo
        BigDecimal total = blocks.stream()
                .map(ExportData.ShopBlock::balance)
                .reduce(Decimals.MONEY_ZERO, BigDecimal::add);

        Row bannerRow = ExcelCells.row(sheet, 1, SUMMARY_BIG_ROW_HEIGHT);
        ExcelCells.merged(sheet, bannerRow, 1, 2, "Saldo", styles.summaryBanner());

        Row totalRow = ExcelCells.row(sheet, 2, SUMMARY_BIG_ROW_HEIGHT);
        ExcelCells.number(totalRow, 1, total, styles.summaryTotal());
        ExcelCells.text(totalRow, 2, null, styles.summaryTotal());
        ExcelCells.mergeRegion(sheet, 2,1,2);

        // Titulo y encabezados de cada grupo
        Row groupRow = ExcelCells.row(sheet, 4, SUMMARY_BIG_ROW_HEIGHT);
        Row headerRow = ExcelCells.row(sheet, 5, SUMMARY_BIG_ROW_HEIGHT);
        for (int g = 0; g < groups; g++) {
            int first = g * SUMMARY_STRIDE;
            ExcelCells.merged(sheet, groupRow, first, first + 2, "Clientes", styles.summaryGroup());
            ExcelCells.text(headerRow, first, "N°", styles.summaryColumnHeader());
            ExcelCells.text(headerRow, first + 1, "Nombre", styles.summaryColumnHeader());
            ExcelCells.text(headerRow, first + 2, "Saldo Pendiente", styles.summaryColumnHeader());
        }

        // Un renglon por sucursal (o por cliente, si tiene una sola), de a 10 por grupo
        for (int i = 0; i < blocks.size(); i++) {
            ExportData.ShopBlock block = blocks.get(i);
            int first = (i / SUMMARY_GROUP_SIZE) * SUMMARY_STRIDE;
            Row row = ExcelCells.row(sheet, 6 + i % SUMMARY_GROUP_SIZE, SUMMARY_ROW_HEIGHT);

            ExcelCells.number(row, first, i + 1, styles.summaryNumber());
            ExcelCells.text(row, first + 1, block.title(), styles.summaryName());
            ExcelCells.number(row, first + 2, block.balance(), styles.summaryBalance());
        }
    }
}

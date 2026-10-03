package org.sistemagestion.backend.service.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sistemagestion.backend.model.enums.SupplierType;

import java.util.HashMap;
import java.util.Map;

// Estilos de las planillas
// Un ExcelStyles por libro: en POI los estilos pertenecen a un libro y se reutilizan
final class ExcelStyles {

    // --------- Medidas ------------

    static final int TITLE_ROW_HEIGHT = 40;     // puntos
    static final int HEADER_ROW_HEIGHT = 25;
    static final int BODY_ROW_HEIGHT = 20;

    // Ancho de columnas, en caracteres:

    // Fecha | Tipo | Corte | Unidad | Kilos | Precio | Entregado | Monto Total | Saldo
    static final double[] CLIENT_WIDTHS = {16, 23, 26, 11, 21, 21, 23, 23, 26};

    // Fecha | Tipo | Unidad | Kilos | Precio | Debe | Haber | Saldo
    static final double[] SUPPLIER_WIDTHS = {18,31,11,18,22,22,22,27};

    // POI mide el ancho en 1/256 de caracter
    static int columnWidth(double characters) {
        return (int) (characters * 256);
    }

    // --------- Colores ------------

    // Paleta de una hoja de movimientos
    enum Look {

        // Clientes: el titulo alterna entre sucursales
        CLIENT(new String[]{"156082", "215F9A"}, "FFFFFF", "0E2841", "FFFFFF", "D9D9D9", "FFC000"),

        // Proveedores bovinos (y "Ambos")
        SUPPLIER_BLUE(new String[]{"0E2841"}, "E8E8E8", "215F9A", "E8E8E8", "E8E8E8", "A6CAEC"),

        // Proveedores porcinos
        SUPPLIER_GREEN(new String[]{"3B7D23"}, "FFFFFF", "275417", "FFFFFF", "E8E8E8", "B4E5A2");

        private final String[] titleFills;
        private final String titleText;
        private final String headerFill;
        private final String headerText;
        private final String balanceFill;
        private final String highlightFill;

        Look(String[] titleFills, String titleText, String headerFill, String headerText, String balanceFill, String highlightFill) {
            this.titleFills = titleFills;
            this.titleText = titleText;
            this.headerFill = headerFill;
            this.headerText = headerText;
            this.balanceFill = balanceFill;
            this.highlightFill = highlightFill;
        }

        static Look forSupplier(SupplierType type) {
            return type == SupplierType.PORCINE ? SUPPLIER_GREEN : SUPPLIER_BLUE;
        }
    }

    // Tipo de dato de una celda del cuerpo
    enum CellKind { TEXT, DATE, UNIT, KILOS, MONEY, BALANCE}

    // --------- Formato de numero ------------

    private static final String GENERAL = "General";
    private static final String UNIT_FORMAT = "0";
    private static final String KILOS_FORMAT = "#,##0.00;-#,##0.00";
    private static final String MONEY_FORMAT = "\"$\"\\ #,##0.00";
    // Fecha corte de Excel (indice 14): se ve segun la region, dd/mm/aaaa en Argentina
    private static final String DATE_FORMAT = "m/d/yy";

    private static final String BLACK = "000000";
    private static final String WHITE = "FFFFFF";

    private final XSSFWorkbook workbook;
    private final DataFormat format;
    private final Map<String, CellStyle> cache = new HashMap<>();

    ExcelStyles(XSSFWorkbook workbook) {
        this.workbook = workbook;
        this.format = workbook.createDataFormat();
    }

    // --------- Hojas de movimientos (clientes y proveedores) ------------

    // Titulo de la hoja o del bloque de una sucursal
    CellStyle title(Look look, int blockIndex) {
        String fill = look.titleFills[blockIndex % look.titleFills.length];

        return style(fill, "Arial", 18, true, look.titleText, GENERAL);
    }

    // Encabezado de columnas (la columna Saldo va un poco mas grande)
    CellStyle header(Look look, boolean balanceColumn) {
        return style(look.headerFill, "Calibri", balanceColumn ? 14 : 13, true, look.headerText, GENERAL);
    }

    // Celda del cuerpo: las filas "highlighted" van pintadas
    CellStyle body(Look look, CellKind kind, boolean highlighted) {

        boolean balance = kind == CellKind.BALANCE;
        String fill = highlighted ? look.highlightFill : (balance ? look.balanceFill : null);
        String numberFormat = switch (kind) {
            case TEXT -> GENERAL;
            case DATE -> DATE_FORMAT;
            case UNIT -> UNIT_FORMAT;
            case KILOS -> KILOS_FORMAT;
            case MONEY, BALANCE -> MONEY_FORMAT;
        };

        return style(fill, "Calibri", balance ? 14: 12, balance, BLACK, numberFormat);

    }

    // --------- Hoja Resumen ------------

    // Cartel "Saldo" de arriba
    CellStyle summaryBanner() {
        return style("156082", "Arial", 16, true, WHITE, GENERAL);
    }

    // Saldo total
    CellStyle summaryTotal() {
        return style("E8E8E8", "Calibri", 18, true, BLACK, MONEY_FORMAT);
    }

    // "Clientes" (encabezado de cada grupo de columnas)
    CellStyle summaryGroup() {
        return style("196B24", "Arial", 16, true, WHITE, GENERAL);
    }

    // N° | Nombre | Saldo Pendiente
    CellStyle summaryColumnHeader() {
        return style("00B050", "Calibri", 16, true, WHITE, GENERAL);
    }

    CellStyle summaryNumber() {
        return style("00B050", "Calibri", 16, true, BLACK, GENERAL);
    }

    CellStyle summaryName() {
        return style("84E291", "Calibri", 16, true, BLACK, GENERAL);
    }

    CellStyle summaryBalance() {
        return style(null, "Calibri", 16, true, BLACK, MONEY_FORMAT);
    }

    // --------- Fabrica ------------

    // Todas las celdas: bordes finos y texto centrado.
    // Se reutiliza el mismo estilo para los mismos parametros
    private CellStyle style(String fill, String fontName, int size, boolean bold, String textColor, String numberFormat)  {

        String key = String.join("|", String.valueOf(fill), fontName, String.valueOf(size),
                String.valueOf(bold), textColor, numberFormat);

        return cache.computeIfAbsent(key, k -> {

            XSSFCellStyle style = workbook.createCellStyle();
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setAlignment(HorizontalAlignment.CENTER);
            if (fill != null) {
                style.setFillForegroundColor(color(fill));
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            XSSFFont font = workbook.createFont();
            font.setFontName(fontName);
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(color(textColor));
            style.setFont(font);

            style.setDataFormat(format.getFormat(numberFormat));
            return style;
        });
    }

    // "RRGGBB" --> color de POI
    private static XSSFColor color(String hex) {
        byte[] rgb = {
                (byte) Integer.parseInt(hex.substring(0,2), 16),
                (byte) Integer.parseInt(hex.substring(2,4), 16),
                (byte) Integer.parseInt(hex.substring(4,6), 16)
        };

        return new XSSFColor(rgb, null);
    }

}

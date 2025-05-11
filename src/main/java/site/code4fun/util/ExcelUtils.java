package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Handle export XLSX file
 * @author TrungTQ
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class ExcelUtils {

    /**
     * Use to export collection of any object using headerMapping to extract field on one sheet
     * @param data collection of data
     * @param headersMapping mapping object field - column title
     * @return byte[]
     */
    public static ByteArrayResource export(final Collection<?> data,
                                           @NonNull final Map<String, String> headersMapping){
        return export(data, headersMapping, null);
    }

    /**
     * Use to export collection of any object using headerMapping to extract field on one sheet
     * @param data collection of data
     * @param headersMapping mapping object field - column title
     * @param resizeCols list of fieldName need resize fit to cell(should avoid decrease performance)
     * @return byte[]
     */
    @SneakyThrows
    public static ByteArrayResource export(final Collection<?> data,
                                           @NonNull final Map<String, String> headersMapping,
                                           final Collection<String> resizeCols) {
        if (data == null || headersMapping.isEmpty()) {
            throw new IllegalArgumentException("Data collection and headers mapping cannot be null or empty.");
        }

        long start = System.currentTimeMillis();
        log.info("Start export");
        Map<String, Method> mapMethod = cacheMethods(data, headersMapping);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet 1");

            handleSheet(data, headersMapping, resizeCols, sheet, buildHeaderStyle(workbook), mapMethod);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            log.info("Done in {}ms", System.currentTimeMillis() - start);
            return new ByteArrayResource(bos.toByteArray());
        }
    }

    private static void autosizeColumn(Sheet sheet, Map<Integer, String> mapFieldOrder, Collection<String> resizeCols) {
        if (!isEmpty(resizeCols)){
            resizeCols.parallelStream().forEach(col -> mapFieldOrder.forEach((index, colName) ->{
                if (col.equalsIgnoreCase(colName)){
                    sheet.autoSizeColumn(index);
                }
            }));
        }
    }

    /**
     * Extract value from object field, can re-calculate by using transient get method<br/>
     * Example use field calculate to get value from getCalculate():
     * <pre>
     *     var calculate = getCalculate();
     * </pre>
     */
    private static void handleSheet(final Collection<?> data,
                                    @NonNull final Map<String, String> headersMapping,
                                    final Collection<String> resizeCols,
                                    Sheet sheet, CellStyle cellStyle,
                                    Map<String, Method> mapMethod){

        Row header = sheet.createRow(0);
        int columnCount = 0;
        Map<Integer, String> mapFieldOrder = new HashMap<>();
        for (String key: mapMethod.keySet()) {
            mapFieldOrder.put(columnCount, key);
            Cell cell = header.createCell(columnCount++, CellType.STRING);
            cell.setCellValue(headersMapping.get(key));
            cell.setCellStyle(cellStyle);
        }
        if (isEmpty(data))  return;

        // Populate data
        AtomicInteger counter = new AtomicInteger(1);
        data.forEach(item ->{
            Row row = sheet.createRow(counter.get());
            mapFieldOrder.forEach((order, fieldName) ->{
                try {
                    Cell cell = row.createCell(order, CellType.STRING);
                    cell.setCellValue(String.valueOf(mapMethod.get(fieldName).invoke(item)));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
            counter.getAndIncrement();
        });
        autosizeColumn(sheet, mapFieldOrder, resizeCols);
    }

    private static CellStyle buildHeaderStyle(XSSFWorkbook workbook){
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
        headerStyle.setFont(font);
        return headerStyle;
    }

    private static Map<String, Method> cacheMethods(Collection<?> data, Map<String, String> headersMapping) {
        Map<String, Method> mapMethod = new HashMap<>();
        Optional<?> optional = data.stream().findFirst();
        optional.ifPresent(obj -> {
            for (Method method : obj.getClass().getMethods()) {
                headersMapping.keySet().forEach(key -> {
                    if (method.getName().replace("get", "").replace("is", "").equalsIgnoreCase(key)) {
                        mapMethod.put(key, method);
                    }
                });
            }
        });
        return mapMethod;
    }
}

package com.vtp.vipo.seller.common.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Service
public class ExcelServiceUtils {

    public <T> ByteArrayInputStream export(List<T> dataList, Class<T> clazz) {
        if (CollectionUtils.isEmpty(dataList) || dataList.isEmpty()) {
            throw new IllegalArgumentException("The data list must not be null or empty");
        }

        if (clazz == null) {
            throw new IllegalArgumentException("The class type must not be null");
        }

        try {
            Workbook workbook = new XSSFWorkbook();
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try {
                    Sheet sheet = workbook.createSheet("Data");

                    // Create header row
                    Field[] fields = clazz.getDeclaredFields();
                    if (fields.length == 0) {
                        throw new IllegalArgumentException("The class type must have at least one field");
                    }

                    Row headerRow = sheet.createRow(0);
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(fields[i].getName());
                    }

                    // Create data rows
                    int rowIdx = 1;
                    for (T data : dataList) {
                        Row row = sheet.createRow(rowIdx++);
                        for (int i = 0; i < fields.length; i++) {
                            fields[i].setAccessible(true);
                            Cell cell = row.createCell(i);
                            try {
                                Object value = fields[i].get(data);
                                if (value != null) {
                                    cell.setCellValue(value.toString());
                                }
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Failed to access field value: " + e.getMessage(), e);
                            }
                        }
                    }

                    workbook.write(out);
                    return new ByteArrayInputStream(out.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    out.close();
                }
            } finally {
                workbook.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file: " + e.getMessage(), e);
        }
    }
}

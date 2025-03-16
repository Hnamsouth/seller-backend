package com.vtp.vipo.seller.common.utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.SimpleDateFormat;
/**
 * Author: hieuhm12
 * Date: 9/23/2024
 */
public class ExcelUtils {
    /**
     * Lấy giá trị của một ô Excel dưới dạng chuỗi.
     *
     * @param cell Ô Excel cần lấy giá trị
     * @return Giá trị chuỗi của ô hoặc null nếu ô trống
     */
    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Kiểm tra nếu giá trị số là số nguyên
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Xử lý ô có kiểu dữ liệu là ngày
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return dateFormat.format(cell.getDateCellValue());
                } else {
                    // Kiểm tra nếu là số nguyên hay số thập phân
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int) numericValue); // Số nguyên
                    } else {
                        return String.valueOf(numericValue); // Số thập phân
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Nếu là công thức, lấy kết quả từ công thức
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                throw new IllegalStateException("Unexpected cell type: " + cell.getCellType());
        }
    }
}

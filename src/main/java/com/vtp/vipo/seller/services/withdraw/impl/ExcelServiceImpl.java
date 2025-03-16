package com.vtp.vipo.seller.services.withdraw.impl;

import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.FeeColumn;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.FeeMap;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestDetailResponse;
import com.vtp.vipo.seller.common.dto.response.withdrawalrequest.WithdrawalRequestItem;
import com.vtp.vipo.seller.common.utils.DateUtils;
import com.vtp.vipo.seller.services.withdraw.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    @Override
    public ByteArrayOutputStream createExcelFile(WithdrawalRequestDetailResponse detail) throws IOException {
        // 1. Tạo workbook & sheet
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        // ========== Chuẩn bị style ==========
        XSSFCellStyle headerStyle = createHeaderStyleWithBorder(workbook);    // Header: nền xanh, viền
        XSSFCellStyle dataStyle = createDataCellStyle(workbook);             // Data text: viền, align left
        XSSFCellStyle dataCurrencyStyle = createDataCurrencyStyle(workbook); // Data tiền: viền, format "#,##0"
        XSSFCellStyle noColorWrapStyle = createNoColorWithBorderStyle(workbook);
        noColorWrapStyle.setWrapText(true);  // Phần đầu/cuối: viền, không màu, xuống dòng
        XSSFCellStyle colorWrapStyle = createColorWithBorderStyle(workbook);
        colorWrapStyle.setWrapText(true);    // Phần giữa: viền, màu, xuống dòng

        // 2. Chuẩn bị feeColumns
        List<FeeColumn> feeColumns = new ArrayList<>(detail.getFeeColumns());
        int feeCount = feeColumns.size();

        // 3. Xác định cột
        //    0: STT
        //    1: Mã đơn
        //    2: Mã vận đơn
        //    3: Ngày giao hàng
        //    4: Số sản phẩm
        //    5: Tiền hàng
        //    6..(6+feeCount-1): cột phí
        //    profitColIndex = 6 + feeCount
        int profitColIndex = 6 + feeCount;
        int lastColIndex = profitColIndex; // cột cuối = profitColIndex

        // ========== Phần đầu (gộp ô) ==========

        // Row 0: "Trạng thái: ...\nLý do: ..."
        CellRangeAddress regionStatusReason = new CellRangeAddress(0, 0, 0, lastColIndex);
        sheet.addMergedRegion(regionStatusReason);
        XSSFRow row0 = sheet.createRow(0);
        XSSFCell cellStatusReason = row0.createCell(0);
        cellStatusReason.setCellStyle(noColorWrapStyle);

        String statusDesc = (detail.getWithdrawalRequestStatusDesc() != null)
                ? detail.getWithdrawalRequestStatusDesc()
                : (detail.getWithdrawalRequestStatus() != null
                ? detail.getWithdrawalRequestStatus().name()
                : "");
        String reason = (detail.getReason() != null) ? detail.getReason() : "";
        cellStatusReason.setCellValue("Trạng thái: " + statusDesc + "\nLý do: " + reason);
        row0.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 2);

        // Kẻ viền vùng gộp
        RegionUtil.setBorderBottom(BorderStyle.THIN, regionStatusReason, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, regionStatusReason, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, regionStatusReason, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, regionStatusReason, sheet);

        // Row 1: "Ngày: ...\nTài khoản: ..."
        CellRangeAddress regionDateAccount = new CellRangeAddress(1, 1, 0, lastColIndex);
        sheet.addMergedRegion(regionDateAccount);
        XSSFRow row1 = sheet.createRow(1);
        XSSFCell cellDateAccount = row1.createCell(0);
        cellDateAccount.setCellStyle(noColorWrapStyle);

        String dateStr = DateUtils.convertEpochToDateString(detail.getCreateAt(), "dd/MM/yyyy");
        String accInfo = (detail.getAccountInfo() != null) ? detail.getAccountInfo() : "";
        cellDateAccount.setCellValue("Ngày: " + dateStr + "\nTài khoản: " + accInfo);
        row1.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 2);

        RegionUtil.setBorderBottom(BorderStyle.THIN, regionDateAccount, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, regionDateAccount, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, regionDateAccount, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, regionDateAccount, sheet);

        // ========== Tạo Header Bảng (row 3) ==========

        int headerRowIndex = 3;
        XSSFRow headerRow = sheet.createRow(headerRowIndex);

        // 0 - STT
        XSSFCell cellSttHeader = headerRow.createCell(0);
        cellSttHeader.setCellValue("STT");
        cellSttHeader.setCellStyle(headerStyle);

        // 1 - Mã đơn
        XSSFCell cellOrderCodeHeader = headerRow.createCell(1);
        cellOrderCodeHeader.setCellValue("Mã đơn");
        cellOrderCodeHeader.setCellStyle(headerStyle);

        // 2 - Mã vận đơn
        XSSFCell cellShipHeader = headerRow.createCell(2);
        cellShipHeader.setCellValue("Mã vận đơn");
        cellShipHeader.setCellStyle(headerStyle);

        // 3 - Ngày giao
        XSSFCell cellSuccessDateHeader = headerRow.createCell(3);
        cellSuccessDateHeader.setCellValue("Ngày giao hàng");
        cellSuccessDateHeader.setCellStyle(headerStyle);

        // 4 - Số sản phẩm
        XSSFCell cellQtyHeader = headerRow.createCell(4);
        cellQtyHeader.setCellValue("Số sản phẩm");
        cellQtyHeader.setCellStyle(headerStyle);

        // 5 - Tiền hàng
        XSSFCell cellAmountHeader = headerRow.createCell(5);
        cellAmountHeader.setCellValue("Tiền hàng");
        cellAmountHeader.setCellStyle(headerStyle);

        // Cột phí => 6..(5 + feeCount)
        Map<String, Integer> feeCodeToIndex = new HashMap<>();
        int startFeeIndex = 6;
        if (!feeColumns.isEmpty()) {
            for (int i = 0; i < feeColumns.size(); i++) {
                FeeColumn fc = feeColumns.get(i);
                int col = startFeeIndex + i;  // 6,7,8...
                feeCodeToIndex.put(fc.getCode(), col);

                XSSFCell cellFeeHeader = headerRow.createCell(col);
                cellFeeHeader.setCellValue(fc.getName());  // hiển thị tên phí
                cellFeeHeader.setCellStyle(headerStyle);
            }
        }

        // "Lợi nhuận ước tính" => profitColIndex
        XSSFCell cellProfitHeader = headerRow.createCell(profitColIndex);
        cellProfitHeader.setCellValue("Lợi nhuận ước tính");
        cellProfitHeader.setCellStyle(headerStyle);

        // ========== Ghi dữ liệu (row 4...) ==========
        int dataRowIndex = headerRowIndex + 1; // row 4
        if (ObjectUtils.isNotEmpty(detail.getWithdrawalRequestItems())) {
            int sttVal = 1;
            for (WithdrawalRequestItem item : detail.getWithdrawalRequestItems()) {
                XSSFRow rowData = sheet.createRow(dataRowIndex++);

                // cột 0 - STT
                XSSFCell cellStt = rowData.createCell(0);
                cellStt.setCellValue(sttVal++);
                cellStt.setCellStyle(dataStyle);

                // cột 1 - Mã đơn
                XSSFCell cellOrder = rowData.createCell(1);
                cellOrder.setCellValue(
                        item.getOrderPackageCode() != null ? item.getOrderPackageCode() : ""
                );
                cellOrder.setCellStyle(dataStyle);

                // cột 2 - Mã vận đơn
                XSSFCell cellShip = rowData.createCell(2);
                cellShip.setCellValue(
                        item.getShippingCode() != null ? item.getShippingCode() : ""
                );
                cellShip.setCellStyle(dataStyle);

                // cột 3 - Ngày giao hàng
                String successDateStr2 = "";
                if (item.getSuccessDeliveryDate() != null) {
                    successDateStr2 = DateUtils.convertEpochToDateString(item.getSuccessDeliveryDate(), "dd/MM/yyyy");
                }
                XSSFCell cellSuccess = rowData.createCell(3);
                cellSuccess.setCellValue(successDateStr2);
                cellSuccess.setCellStyle(dataStyle);

                // cột 4 - Số sản phẩm
                long qtyVal = (item.getQuantity() != null) ? item.getQuantity() : 0;
                XSSFCell cellQty = rowData.createCell(4);
                cellQty.setCellValue(qtyVal);
                cellQty.setCellStyle(dataStyle);

                // cột 5 - Tiền hàng
                double amountVal = (item.getOrderAmount() != null) ? item.getOrderAmount().doubleValue() : 0;
                XSSFCell cellAmount = rowData.createCell(5);
                cellAmount.setCellValue(amountVal);
                cellAmount.setCellStyle(dataCurrencyStyle);

                // cột phí => feeCodeToIndex
                if (ObjectUtils.isNotEmpty(item.getPlatformFees())) {
                    for (FeeMap pf : item.getPlatformFees()) {
                        if (pf.getColumnCode() == null) continue;
                        Integer feeCol = feeCodeToIndex.get(pf.getColumnCode());
                        if (feeCol != null) {
                            double feeVal = (pf.getFeeValue() != null) ? pf.getFeeValue().doubleValue() : 0;
                            XSSFCell cellFee = rowData.createCell(feeCol);
                            cellFee.setCellValue(feeVal);
                            cellFee.setCellStyle(dataCurrencyStyle);
                        }
                    }
                }

                // cột Lợi nhuận => profitColIndex
                double profitVal = (item.getEstimatedProfit() != null)
                        ? item.getEstimatedProfit().doubleValue()
                        : 0;
                XSSFCell cellProfit = rowData.createCell(profitColIndex);
                cellProfit.setCellValue(profitVal);
                cellProfit.setCellStyle(dataCurrencyStyle);
            }
        }

        // ========== Tạo dòng cuối (Tổng đơn, Thuế, Tổng tiền rút) ==========

        dataRowIndex += 1;  // chừa 1 dòng
        XSSFRow finalRow = sheet.createRow(dataRowIndex);

        // Gộp cột 0..(profitColIndex-1) => "Tổng đơn: x"
        CellRangeAddress regionTotalOrder = new CellRangeAddress(dataRowIndex, dataRowIndex, 0, profitColIndex - 1);
        sheet.addMergedRegion(regionTotalOrder);

        XSSFCell cellTotalOrder = finalRow.createCell(0);
        cellTotalOrder.setCellStyle(colorWrapStyle);

        int totalItem = (detail.getWithdrawalRequestItems() != null)
                ? detail.getWithdrawalRequestItems().size()
                : 0;
        cellTotalOrder.setCellValue("Tổng đơn: " + totalItem);

        // Kẻ viền
        RegionUtil.setBorderBottom(BorderStyle.THIN, regionTotalOrder, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, regionTotalOrder, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, regionTotalOrder, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, regionTotalOrder, sheet);

        // Cột profitColIndex => "Thuế: ???\nTổng tiền rút: ???"
        XSSFCell cellTaxTotal = finalRow.createCell(profitColIndex);
        cellTaxTotal.setCellStyle(colorWrapStyle);

        String taxStr = (detail.getTax() != null)
                ? NumberFormat.getInstance().format(detail.getTax().doubleValue())
                : "0";
        String totalWithdrawalStr = (detail.getTotalWithdrawal() != null)
                ? NumberFormat.getInstance().format(detail.getTotalWithdrawal().doubleValue())
                : "0";
        cellTaxTotal.setCellValue("Thuế: " + taxStr + "\nTổng tiền rút: " + totalWithdrawalStr);

        finalRow.setHeightInPoints(sheet.getDefaultRowHeightInPoints() * 2);

        // Kẻ viền cột cuối
        CellRangeAddress regionTaxTotal = new CellRangeAddress(dataRowIndex, dataRowIndex, profitColIndex, profitColIndex);
        RegionUtil.setBorderBottom(BorderStyle.THIN, regionTaxTotal, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN, regionTaxTotal, sheet);
        RegionUtil.setBorderLeft(BorderStyle.THIN, regionTaxTotal, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN, regionTaxTotal, sheet);

        // ========== Auto-size cột 0..profitColIndex ==========
        // (Tổng số cột = profitColIndex + 1)
        for (int i = 0; i <= profitColIndex; i++) {
            sheet.autoSizeColumn(i);
        }

        // ========== Ghi workbook ra ByteArrayOutputStream ==========
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos;
    }

    // Style border
    private XSSFCellStyle createBorderedCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    // Style header (border, color, bold, center)
    private XSSFCellStyle createHeaderStyleWithBorder(XSSFWorkbook workbook) {
        XSSFCellStyle style = createBorderedCellStyle(workbook);

        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    // Style data text (border, align left)
    private XSSFCellStyle createDataCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = createBorderedCellStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // Style data currency (border, align right, format "#,##0")
    private XSSFCellStyle createDataCurrencyStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = createBorderedCellStyle(workbook);

        DataFormat df = workbook.createDataFormat();
        style.setDataFormat(df.getFormat("#,##0"));

        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // Style no color + border + align left (after setWrapText(true) if needed)
    private XSSFCellStyle createNoColorWithBorderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = createBorderedCellStyle(workbook);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    // Style màu + border + align left (after setWrapText(true) if needed)
    private XSSFCellStyle createColorWithBorderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = createBorderedCellStyle(workbook);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}

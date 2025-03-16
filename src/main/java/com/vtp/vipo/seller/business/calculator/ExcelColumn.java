package com.vtp.vipo.seller.business.calculator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
//todo: rename for more valuable name
public enum ExcelColumn {
    MA_DON_HANG(0, "Mã đơn hàng", OrderPackageExportRowDTO::getOrderCode, 7680),
//    MA_KIEN_HANG(1, "Mã kiện hàng", OrderPackageExportRowDTO::getMaKienHang, 7680),
    NGAY_DAT_HANG(1, "Ngày đặt hàng", OrderPackageExportRowDTO::getDeliveryStartTime, 7680),
    TRANG_THAI_DON_HANG(2, "Trạng thái đơn hàng", OrderPackageExportRowDTO::getSellerOrderStatusName, 7680),
    LUU_Y_TU_NGUOI_MUA(3, "Lưu ý từ người mua", OrderPackageExportRowDTO::getNoteSeller, 7680),
    MA_VAN_DON(4, "Mã vận đơn", OrderPackageExportRowDTO::getShipmentCode, 7680),
    DON_VI_VAN_CHUYEN(5, "Đơn vị vận chuyển", OrderPackageExportRowDTO::getCarrierName, 7680),
    PHUONG_THUC_GIAO_HANG(6, "Phương thức giao hàng", OrderPackageExportRowDTO::getShipmentMethod, 7680),
    NGAY_GIAO_HANG_DUKIEN(7, "Ngày giao hàng dự kiến", OrderPackageExportRowDTO::getExpectedDeliveryTime, 7680),
    LOAI_DON_HANG(8, "Loại đơn hàng", OrderPackageExportRowDTO::getOrderType, 7680),
    NGAY_GUI_HANG(9, "Ngày gửi hàng", OrderPackageExportRowDTO::getShipmentDate, 7680),
    THOI_GIAN_GIAO_HANG(10, "Thời gian giao hàng", OrderPackageExportRowDTO::getDeliverySuccessTime, 7680),
    SKU_SAN_PHAM(11, "SKU sản phẩm", OrderPackageExportRowDTO::getSpecMap, 7680),
    TEN_SAN_PHAM(12, "Tên sản phẩm", OrderPackageExportRowDTO::getSkuName, 7680),
    CAN_NANG_SAN_PHAM(13, "Cân nặng sản phẩm", OrderPackageExportRowDTO::getSkuWeight, 7680),
    TONG_CAN_NANG(14, "Tổng cân nặng", OrderPackageExportRowDTO::getTotalSkuWeight, 7680),
    SKU_PHAN_LOAI_HANG(15, "SKU phân loại hàng", OrderPackageExportRowDTO::getCategoryCode, 7680),
    TEN_PHAN_LOAI_HANG(16, "Tên phân loại hàng", OrderPackageExportRowDTO::getCategoryName, 7680),
    GIA_GOC_1(17, "Giá gốc (1)", OrderPackageExportRowDTO::getOriginTotalSkuPriceWithoutPriceRange, 7680),
    NGUOI_BAN_TRO_GIA_2(18, "Người bán trợ giá (2)", OrderPackageExportRowDTO::getPriceRangeDeduction, 7680),
    TONG_GIA_BAN_3(19, "Tổng giá bán (3)", OrderPackageExportRowDTO::getOriginTotalSkuPrice, 7680),
    TIEN_DAM_PHAN_TREN_SKU_4(20, "Tiền đàm phán trên SKU (4)", OrderPackageExportRowDTO::getNegotiatedDeductionAmountOnSku, 7680),
    TIEN_DAM_PHAN_TREN_TONG_DON_HANG_5(21, "Tiền đàm phán trên tổng đơn hàng (5)", OrderPackageExportRowDTO::getTotalNegotiatedDeductionAmount, 7680),
    REF_CODE(22, "ref code", OrderPackageExportRowDTO::getRefCode, 7680),
    SO_LUONG(23, "Số lượng", OrderPackageExportRowDTO::getTotalSkuQuantity, 7680),
    TONG_GIA_TRI_DON_HANG_VND_6(24, "Tổng giá trị đơn hàng (VND) (6)", OrderPackageExportRowDTO::getTotalSkuPrice, 7680),
    PHI_CHIET_KHAU_SAN_7(25, "Phí chiết khấu sàn (7)", OrderPackageExportRowDTO::getTotalPlatformDiscountFromProduct, 7680),
    PHI_VAN_CHUYEN_DUKIEN_8(26, "Phí vận chuyển (dự kiến)(8)", OrderPackageExportRowDTO::getTotalShippingFee, 7680),
    PHI_VAN_CHUYEN_NGUOI_MUA_TRA_9(27, "Phí vận chuyển mà người mua trả (9)", OrderPackageExportRowDTO::getTotalDomesticShippingFee, 7680),
    TONG_SO_TIEN_NGUOI_MUA_THANH_TOAN_10(28, "Tổng số tiền người mua thanh toán (10)", OrderPackageExportRowDTO::getTotalPaidBuyer, 7680),
    TIEN_DA_COC_11(29, "Tiền đã cọc (11)", OrderPackageExportRowDTO::getPrepayment, 7680),
    THOI_GIAN_HOAN_THANH_DON_HANG(30, "Thời gian hoàn thành đơn hàng", OrderPackageExportRowDTO::getDeliverySuccessTime, 7680),
    THOI_GIAN_DON_HANG_DUOC_THANH_TOAN(31, "Thời gian đơn hàng được thanh toán", OrderPackageExportRowDTO::getDeliverySuccessTime, 7680),
    PHUONG_THUC_THANH_TOAN(32, "Phương thức thanh toán", OrderPackageExportRowDTO::getPaymentMethod, 7680),
    PHI_SAN_DYNAMIC(33, "Phí cố định (12)", (row) -> "", 7680),
    DOANH_THU_DUKIEN_TREN_DON_14(34, "Doanh thu dự kiến trên đơn (15) = (6) - (7)", OrderPackageExportRowDTO::getExpectedRevenue, 7680),
    NGUOI_MUA(35, "Người Mua", OrderPackageExportRowDTO::getCustomerName, 7680),
    TEN_NGUOI_NHAN(36, "Tên Người nhận", OrderPackageExportRowDTO::getReceiverName, 7680),
    SO_DIEN_THOAI(37, "Số điện thoại", OrderPackageExportRowDTO::getReceiverPhone, 7680),
    TINH_THANH_PHO(38, "Tỉnh/Thành phố", OrderPackageExportRowDTO::getReceiverProvince, 7680),
    TP_QUAN_HUYEN(39, "TP / Quận / Huyện", OrderPackageExportRowDTO::getReceiverDistrict, 7680),
    PHUONG(40, "Phường", OrderPackageExportRowDTO::getReceiverWard, 7680),
    DIA_CHI_NHAN_HANG(41, "Địa chỉ nhận hàng", OrderPackageExportRowDTO::getReceiverAddress, 7680),
    QUOC_GIA(42, "Quốc gia", OrderPackageExportRowDTO::getCountryName, 7680),
    GHI_CHU_NHA_BAN(43, "Ghi chú nhà bán", OrderPackageExportRowDTO::getSellerNoteForShipment, 7680);


    private final int index;
    private final String headerName;
    private final Function<OrderPackageExportRowDTO, Object> valueExtractor;
    private final int columnWidth;

    public final static List<ExcelColumn> UPDATED_COLUMN_WHEN_SUM_UP
            = List.of(TONG_CAN_NANG, SO_LUONG, PHI_CHIET_KHAU_SAN_7, DOANH_THU_DUKIEN_TREN_DON_14);

    /**
     * Retrieves the ExcelColumn by its index.
     *
     * @param index The column index.
     * @return The corresponding ExcelColumn enum constant.
     */
    public static ExcelColumn getByIndex(int index) {
        for (ExcelColumn column : values()) {
            if (column.getIndex() == index) {
                return column;
            }
        }
        throw new IllegalArgumentException("Invalid column index: " + index);
    }

}


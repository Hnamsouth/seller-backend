package com.vtp.vipo.seller.common.constants;

public class RevenueConstant {
    public static final String WITHDRAW_NOT_FOUND = "Không tìm thấy yêu cầu rút tiền";

    // Yêu cầu đang ở trạng thái đang xử lý/từ chối. Người dùng không thể hủy
    public static final String CANNOT_CANCEL_PROCESSING_REJECTED = "Yêu cầu đang ở trạng thái đang xử lý/từ chối. Người dùng không thể hủy";

    // Yêu cầu đã bị hủy
    public static final String CANCELLED_REQUEST = "Yêu cầu đã bị hủy";

    // Chỉ được hủy khi trạng thái yêu cầu là "chờ xử lý"
    public static final String CANCEL_ONLY_PENDING = "Chỉ được hủy khi trạng thái yêu cầu là \"chờ xử lý\"";

    // Hủy yêu cầu rút tiền thành công
    public static final String CANCEL_WITHDRAW_SUCCESS = "Bạn đã hủy yêu cầu thành công";

    // Không tìm thấy báo cáo
    public static final String REPORT_NOT_FOUND = "Không tìm thấy báo cáo";

    // "ChiTietYeuCau.xlsx"
    public static final String EXPORT_WITHDRAW_DETAIL_FILE_NAME = "ChiTietYeuCau.xlsx";

    // "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    public static final String EXPORT_WITHDRAW_DETAIL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
}



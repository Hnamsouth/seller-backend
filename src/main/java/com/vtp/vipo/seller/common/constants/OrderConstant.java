package com.vtp.vipo.seller.common.constants;

import java.util.List;

public class OrderConstant {

    public static final List<String> REFUSE_STATUS_BY_BUYER  = List.of("091", "092", "093", "094", "201", "6", "503");

    public static final String CAN_NOT_CANCEL_WHEN_ORDER_CANCELED = "Đơn hàng đã bị hủy không thể thực hiện tác vụ";

    public static final String CAN_NOT_REJECT_WHEN_ORDER_REJECTED = "Đơn hàng đã bị hủy không từ chối";

    public static final String ORDER_CANCEL_COMPLETED = "Hủy đơn hàng thành công";

    public static final String ORDER_REJECT_COMPLETED = "Từ chối đơn hàng thành công";

    public static final String SELLER_ORDER_APPROVED = "Duyệt đơn hàng thành công";

    public static final String ORDER_REJECT_FAILED = "Từ chối hàng thất bại";

    public static final String ORDER_CANCEL_FAILED = "Hủy đơn hàng thất bại";

    public static final String ORDER_CANCELED= "Đơn hàng đã bị hủy";

    public static final String REQUIRED_SELECT_SKU_OFS = "Yêu cầu chọn sản phẩm hết hàng";

    public static final String ORDER_CANCEL_REJECT_REASON_NOT_BLANK = "Yêu cầu nhập lý do %s đơn";

    public static final String SELLER_REJECT_CANCEL_OUT_OF_STOCK = "Nhà bán hết hàng";

    public static final String STATUS_ORDER_CHANGED = "Đơn hàng đã chuyển trạng thái không thể thực hiện hành động này";

    public static final String ORDER_STATUS_SELLER_CANCEL_ORDER = "094";

    public static final String ORDER_STATUS_SELLER_REJECT_ORDER = "023";

    public static final String SELLER_TAB_ALL = "Tất cả";

    public static final String STATUS_CANCELED = "CANCELED";

    public static final String STATUS_REJECTED = "REJECTED";

    public static final String STATUS_FAILED = "FAILED";

    public static final String ORDER_REJECT_NAME = "Từ chối";

    public static final String ORDER_CANCEL_NAME = "Hủy";

    public static final String PRODUCT_PLATFORM_DISCOUNT = "Chiết khấu sàn theo sản phẩm";

    public static final String SELLER_APPROVED_ORDER = "Nhà bán phê duyệt đơn hàng";

    public static final String VIETNAM_MERCHANT_ONLY = "Chỉ dành cho nhà bán Việt Nam";

}

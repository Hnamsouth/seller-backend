package com.vtp.vipo.seller.common.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class OrderReport {
    // 1. Mã đơn hàng
    String orderCode;
    // 2. Mã kiện hàng
    String shipmentCode;
    // 3. Ngày đặt hàng
    Date orderDate;
    // 4. Trạng thái đơn hàng
    String orderStatus;
    // 5. Lưu ý từ người mua
    String buyerNote;
    // 6. Mã vận đơn
    String trackingCode;
    // 7. Đơn vị vận chuyển
    String shippingCompany;
    // 8. Phương thức giao hàng
    String shippingMethod;
    // 9. Ngày giao hàng dự kiến
    Date estimatedDeliveryDate;
    // 10. Ngày gửi hàng
    Date shippingDate;
    // 11. Thời gian giao hàng
    Date deliveryTime;
    // 12. SKU sản phẩm
    String productSKU;
    // 13. Tên sản phẩm
    String productName;
    // 14. Cân nặng sản phẩm
    Double productWeight;
    // 15. Tổng cân nặng
    Double totalWeight;
    // 16. SKU phân loại hàng
    String productCategorySKU;
    // 17. Tên phân loại hàng
    String productCategoryName;
    // 18. Giá gốc
    Double basePrice;
    // 19. Người bán trợ giá
    Double sellerDiscount;
    // 20. Tổng giá bán
    Double totalPrice;
    // 21. Tiền đàm phán trên SKU
    Double negotiationAmountPerSKU;
    // 22. Tiền đàm phán trên tổng đơn hàng
    Double negotiationAmountTotal;
    // 23. Ref code
    String refCode;
    // 24. Số lượng
    Integer quantity;
    // 25. Tổng giá trị đơn hàng (VND)
    Double totalOrderValue;
    // 26. Phí chiết khấu sàn
    Double platformDiscountFee;
    // 27. Phí vận chuyển (dự kiến)
    Double estimatedShippingFee;
    // 28. Phí vận chuyển mà người mua trả
    Double buyerPaidShippingFee;
    // 29. Tổng số tiền người mua thanh toán
    Double totalPaymentByBuyer;
    // 30. Tiền đã cọc
    Double depositPaid;
    // 31. Thời gian hoàn thành đơn hàng
    Date orderCompletionTime;
    // 32. Thời gian đơn hàng được thanh toán
    Date paymentTime;
    // 33. Phương thức thanh toán
    String paymentMethod;
    // 34. Phí cố định
    Double fixedFee;
    // 35. Phí dịch vụ
    Double serviceFee;
    // 36. Phí thanh toán
    Double paymentFee;
    // 37. Doanh thu dự kiến trên đơn
    Double expectedRevenue;
    // 38. Người mua
    String buyerName;
    // 39. Tên Người nhận
    String recipientName;
    // 40. Số điện thoại
    String phoneNumber;
    // 41. Tỉnh/Thành phố
    String province;
    // 42. TP / Quận / Huyện
    String district;
    // 43. Quận
    String commune;
    // 44. Địa chỉ nhận hàng
    String shippingAddress;
    // 45. Quốc gia
    String country;
    // 46. Ghi chú nhà bán
    String sellerNote;

}


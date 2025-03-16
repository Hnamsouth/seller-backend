package com.vtp.vipo.seller.business.calculator;

import com.vtp.vipo.seller.common.dao.entity.enums.packageproduct.PriceRange;
import com.vtp.vipo.seller.common.utils.DataUtils;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderPackageExportRowDTO {

    //todo: bổ sung các trường thiếu khi làm UC09, 10
    /* mã kiện hàng, , ngày giao hàng dự kiến, Người bán trợ giá (2), ghi chú nhà bán  */

    private Long packageId; // package_product.packageId
    private Long id; // package_product.id
    private Long skuId; // package_product.lazbaoSkuId
    private Long skuStock; // product_seller_sku.id
    private String orderCode; // Mã đơn hàng
    private String maKienHang; // mã kiện hàng, no data yet
    private String shipmentCode; // Mã vận đơn
    private LocalDateTime createTime; // Ngày đặt hàng
    private LocalDateTime deliveryStartTime; // Ngày gửi nhà bán
    private String sellerOrderStatusName; // Trạng thái đơn hàng (người bán)
    private String sellerOrderStatusDescription;
    private String sellerOrderStatus;
    private String noteSeller; // Lưu ý từ người mua
    private String orderType; // Loại đơn hàng
    private LocalDateTime expectedDeliverySuccessDate; // expectedDeliveryDate
    private LocalDateTime shipmentDate; // Ngày gửi hàng
    private LocalDateTime deliverySuccessTime; // Thời gian giao hàng
    private String skuName; // Tên sản phẩm
    private Long skuWeight; // Cân nặng các SKU của sản phẩm
    private String categoryName; // Tên phân loại hàng
    private String categoryCode; // Mã phân loại hàng
    private BigDecimal skuPrice; // Giá gốc (1)
    private BigDecimal sellerDiscountPrice; //Người bán trợ giá (2)
    private BigDecimal totalSkuPrice; // Tổng giá bán (3)
    private BigDecimal originTotalSkuPrice; // Tổng giá bán (3)
    private BigDecimal price;
    private BigDecimal totalPrice;  //25	Tổng giá trị đơn hàng (VND) (6)
    private BigDecimal skuNegotiatedAmount; // Tiền đàm phán trên SKU (4)
    private BigDecimal negotiatedAmount; // Tiền đàm phán trên tổng đơn hàng (5)
    private String refCode;
    private Long skuQuantity; // số lượng của sku
    private BigDecimal totalPlatformFee; // Phí chiết khấu sàn (7)
    //todo: Phí vận chuyển (dự kiến)(8)
    private BigDecimal totalShippingFee; // Phí vận chuyển mà người mua trả (9)
    private BigDecimal totalPaidBuyer; // Tổng số tiền người mua thanh toán (10)
    private BigDecimal prepayment; // Tiền đã cọc (11)
    //todo: (12) - (13) - (14)
    private BigDecimal expectedRevenue; // Doanh thu dự kiến trên đơn (15)
    private String customerName; // Người Mua
    private String receiverName; // Tên Người nhận
    private String receiverPhone;
    private String receiverWard;
    private String receiverDistrict;
    private String receiverProvince;
    private String receiverAddress;
    private String carrierName; // đơn vị vận chuyển
    private String shipmentMethod; // phương thức giao hàng
    private String paymentMethod; // phương thức thanh toán
    private String countryName;
    private String specMap; //
    private BigDecimal platformDiscountRate; //26 phí triết khấu sàn
    private BigDecimal sellerPlatformDiscountRate; // phí triết khấu sàn lưu ở package_product
    private BigDecimal sellerPlatformDiscountAmount; // phí triết khấu sàn lưu ở package_product
    private BigDecimal platformDiscountFromProduct;
    private BigDecimal totalDomesticShippingFee;
    private List<BigDecimal> platformFees;
    private Map<Long, BigDecimal> platformFeeMap;
    private List<PriceRange> priceRanges;
    private BigDecimal originTotalSkuPriceWithoutPriceRange;     //giá gốc không có thang giá
    private BigDecimal priceRangeDeduction = BigDecimal.ZERO;                         //người bán trợ giá (qua thang giá)
    private BigDecimal totalNegotiatedDeductionAmount;          //Tiền đàm phán trên tổng đơn hàng (5)
    private BigDecimal negotiatedDeductionAmountOnSku;     //Tiền đàm phán trên SKU (4)
    private String sellerNoteForShipment;
    private LocalDateTime expectedDeliveryTime;
    private Long paymentTime;


    private boolean isFirstSkuRow; //determine the first package product of the order_package
    private Long totalSkuWeight ;
    private Long totalSkuQuantity;
    private BigDecimal totalNegotiatedAmount;
//    private BigDecimal priceMinusNegotiatedAmount;
    private Integer rowNum;
    private BigDecimal totalPlatformDiscountFromProduct;

    public BigDecimal getTotalPrice(){
        return !DataUtils.isNullOrEmpty(this.price) ? this.price.subtract(this.sellerDiscountPrice) : BigDecimal.ZERO;
    }
}

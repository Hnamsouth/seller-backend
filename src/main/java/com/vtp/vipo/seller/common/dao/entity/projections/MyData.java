package com.vtp.vipo.seller.common.dao.entity.projections;

import com.vtp.vipo.seller.common.constants.Constants;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//todo: rename to SellerPackageProductForExportProjection
public interface MyData {


    //todo: bổ sung các trường thiếu khi làm UC09, 10
    /* mã kiện hàng, , ngày giao hàng dự kiến, Người bán trợ giá (2), ghi chú nhà bán  */

    //package_product.packageId
    Long getPackageId();

    //package_product.id
    Long getId();

    //package_product.lazbaoSkuId
    Long getSkuId();

    //product_seller_sku.id
    Long getSkuStock();

    String getOrderCode();          //Mã đơn hàng

    //todo
    String getMaKienHang();     //mã kiện hàng, no data yet

    String getShipmentCode();       //Mã vận đơn

    //todo
//    String getOrderType();       // Loại đơn hàng

    Long getCreateTime();        //Ngày đặt hàng

    Long getDeliveryStartTime();        //Ngày gửi nhà bán

    String getSellerOrderStatusName();  //Trạng thái đơn hàng (người bán)

    String getNoteSeller();         //Lưu ý từ người mua

    //todo
    LocalDateTime getExpectedDeliverySuccessDate(); //expectedDeliveryDate

    LocalDateTime getShipmentDate();      //Ngày gửi hàng

    Long getDeliverySuccessTime();         //Thời gian giao hàng

    String getSkuName();    //Tên sản phẩm

    Long getSkuWeight();    //Cân nặng tổng của tất cả các SKU của sản phẩm

    String getCategoryName();   //Tên phân loại hàng

    String getCategoryCode();   // Mã phân loại hàng

    BigDecimal getSkuPrice();   //Giá gốc (1)

    BigDecimal getTotalSkuPrice();  //Tổng giá bán (3)

    BigDecimal getOriginTotalSkuPrice(); //Tổng tiền hàng ban đầu

    BigDecimal getPrice();          //tổng hiền hàng

    BigDecimal getTotalPrice();  //25	Tổng giá trị đơn hàng (VND) (6)

    BigDecimal getSkuNegotiatedAmount();   //Tiền đàm phán trên SKU (4)

    BigDecimal getNegotiatedAmount();       //Tiền đàm phán trên tổng đơn hàng (5)

    String getRefCode();

    Long getSkuQuantity();  //số lượng của sku, sẽ cộng dồn khi tính tổng số lượng sku trên đơn hàng

    BigDecimal getTotalPlatformFee(); //Phí chiết khấu sàn (7)

    BigDecimal getPrepayment();     //Tiền đã cọc

    BigDecimal getTotalShippingFee();     //Phí vận chuyển người mua trả (9)

    String getCustomerName(); //38	Người Mua

    String getReceiverName();   //39 Tên Người nhận

    String getReceiverPhone();

    String getReceiverWard();

    String getReceiverDistrict();

    String getReceiverProvince();

    String getReceiverAddress();

    String getSpecMap();

    BigDecimal getPlatformDiscountRate(); //phí triết khấu sàn

    BigDecimal getTotalDomesticShippingFee(); //Phí vận chuyển nội địa

    String getPlatformFeeStr(); //Phí chiết khấu sàn

    String getPriceRanges();

    BigDecimal getSellerPlatformDiscountRate();

    BigDecimal getSellerPlatformDiscountAmount();

    String getSellerNoteForShipment();

    LocalDateTime getExpectedDeliveryTime();

    String getSellerOrderStatus();

    Integer getIsChangePrice();

    Long getPaymentTime();

    default String getCarrierName() {            //đơn vị vận chuyển
        return Constants.VTP_CARRIER_NAME;
    }

    default String getShipmentMethod() {        //phương thức giao hàng
        return Constants.DEFAULT_SHIPMENT_METHOD;
    }

    default String getCountryName() {        //phương thức giao hàng
        return Constants.DEFAULT_COUNTRY_NAME;
    }

    default String getPaymentMethod() { return Constants.DEFAULT_PAYMENT_METHOD; }

}

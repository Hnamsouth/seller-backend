package com.vtp.vipo.seller.common.dao.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vtp.vipo.seller.common.dao.entity.enums.SellerOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Table(name = "order_package")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class OrderPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private OrderEntity order;

    private String orderCode;

    private String receiverName;

    private String receiverEmail;

    private String receiverPhone;

    private Long receiverWardId;

    private String receiverAddress;

    private String receiverAddressDetail;

    private BigDecimal totalPrice;

    private BigDecimal internationalShippingFee;

    private String orderStatus;

    private Long quantity;

    private BigDecimal orderValue;

    @OneToMany(mappedBy = "orderPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<PackageProductEntity> sku = new HashSet<>(); //TODO: Rename

    private Long confirmTime;

    private BigDecimal prePayment;

    private BigDecimal totalPaid;

    private String addedServiceMap;

    private Integer productPriceType;

    private Long customerId;

    private Long merchantId;

    private BigDecimal price; //tổng tiền hàng

    private BigDecimal priceRMB; //tổng tiền hàng RMB

    private BigDecimal originPrice; //tổng tiền hàng gốc

    private BigDecimal totalShippingPrice; //tổng tiền ship

    private BigDecimal totalInternationalShippingFee; //tổng cước ship quốc tế

    private BigDecimal totalInternationalShippingFeeRmb; //tổng cước ship quốc tế

    private Long receiverProvinceId;

    private Long receiverDistrictId;

    private BigDecimal chinaDomesticShippingFee;

    private BigDecimal chinaDomesticShippingFeeRMB;

    private BigDecimal proxyPurchasingFee;

    private BigDecimal proxyPurchasingFeeRmb;

    private BigDecimal basicInternationalInspectionFee;

    private BigDecimal basicInternationalInspectionFeeRmb;

    private BigDecimal totalDomesticShippingFee;

    private String noteSeller;

    private String domesticExtraServices;

    private Long paymentTime;

    private Long cancelTime;

    private Integer cancelStatus;

    private String cancelNote;

    private Long cancelByCustomerId;

    private Long pickupTime;

    private String shipmentId;

    private Long createTime;

    private Long updateTime;

    private Long productId;

    /**
     * Status of the seller's order.
     * Mapped to the 'sellerOrderStatus' column in the 'order_package' table.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sellerOrderStatus", nullable = false)
    private SellerOrderStatus sellerOrderStatus;

    private String refCode;

    private BigDecimal negotiatedAmount = BigDecimal.ZERO;

    @Version
    @Column(name = "VERSION")
    private Long version;

    private String sellerOpenId;

    private Boolean isChangePrice;

    private Double receiverLat;

    private Double receiverLon;

    private Double senderLat;

    private Double senderLon;

    private String senderName;

    private String senderPhone;

    private String senderEmail;

    private String senderAddress;

    private Long senderProvinceId;

    private Long senderDistrictId;

    private Long senderWardId;

    private String note;

    @Column(name = "CODPaymentMC")
    private BigDecimal codPaymentMC;

    private BigDecimal totalShippingFee;

    private BigDecimal shippingFee;

    @Column(name = "CODFee")
    private BigDecimal codFee;

    @Column(name = "MoneyFee")
    private BigDecimal moneyFee;

    @Column(name = "OtherFee")
    private BigDecimal otherFee;

    @Column(name = "VASFee")
    private BigDecimal vasFee;

    @Column(name = "VATFee")
    private BigDecimal vatFee;

    private Long makeShipmentTime;

    private Long deliverySuccessTime;

    private BigDecimal estimatedRevenue;
}

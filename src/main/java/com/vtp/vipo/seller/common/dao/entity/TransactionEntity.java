package com.vtp.vipo.seller.common.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long packageId;

    private Long walletId;

    private String vtTransactionId;

    private String provider;

    private BigDecimal price;

    private Integer status;

    private String send;

    private String response;

    private String responseCode;

    private String responseMsg;

    private String gatewayUrl;

    private String checkSum;

    @CreationTimestamp
    private Timestamp createTime;

    @UpdateTimestamp
    private Timestamp updateTime;

    private String type;

    private Long orderCard;

    private String transactionCode;

    private String bankCode;

    private String paymentMethod;

    private BigDecimal customerFee;

    private Long timeTransactionSuccess;

}

package com.vtp.vipo.seller.common.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "merchant_contract_file")
public class MerchantContractFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private Long merchantId;

    @Column
    private String fileLink;

    @Column
    private String fileName;

    @Column
    private Long createTime;

    @Column
    private Integer deleted;

    @Column
    private Boolean deleteTime;

}

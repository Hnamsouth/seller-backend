package com.vtp.vipo.seller.common.dao.entity;

import lombok.*;

import jakarta.persistence.*;

@Table(name = "category")
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long parentId;

    private String name;
    private String code;

    private String icon;

    private String bigIcon;

    private String image;

    private String banner;

    private String bannerMenu;

    private String description;

    private String keywords;

    private Integer position;

    private Integer published;

    private String shipmentMethod;

    private String bannerMobile;

    private Integer consumerCategory;

    private Integer icheckId;

    private Float affiliateMinComission;

    private Float mallCommissionForSystem = -1.00f; // Khởi tạo giá trị mặc định
    @Column(name = "`group`")
    private String group;

    private String languages;

    public CategoryEntity(Long id, Long parentId, String name, String icon, String languages) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.icon = icon;
        this.languages = languages;
    }

    public CategoryEntity() {
        // Khởi tạo mặc định cho mallCommissionForSystem
        this.mallCommissionForSystem = -1.00f;
    }
}

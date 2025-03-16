package com.vtp.vipo.seller.common.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductRequest {

    private Long id;

    private Long parentId = 0L;

    @NotBlank(message = "Please enter supplier")
    @Size(min = 0, max = 150, message = "Supplier must not exceed 50 characters")
    private String supplier;

    @NotNull(message = "Please enter countryName")
    private int countryId;

    @NotBlank(message = "Please enter name")
    @Size(min = 0, max = 155, message = "Name must not exceed 50 characters")
    private String name;

    private String[] images;

    private String[] trailerVideo = null;

    private String description;

    private Long price = 0L;

    private Integer weight = 0;

    private Integer length = 0;

    private Integer width = 0;

    private Integer height = 0;

    private String priceScale="";

    private Long productPropertyId = 0L;

    private Boolean available = false;

    private Long createTime = 0L;

    private Integer viewCount = 0;

    private Integer buyCount = 0;

    private Integer status = 0;

    private int isDeleted = 0;

    private int step = 1;

    private int activated ;

}

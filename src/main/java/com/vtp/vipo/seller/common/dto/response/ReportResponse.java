package com.vtp.vipo.seller.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportResponse {

    @JsonProperty("soLuongTruyCap")
    public Long soLuongTruyCap = 0L;

    @JsonProperty("soNguoiBan")
    public Long soNguoiBan = 0L;

    @JsonProperty("soNguoiBanMoi")
    public Long soNguoiBanMoi = 0L;

    @JsonProperty("tongSoSanPham")
    public Long tongSoSanPham = 0L;

    @JsonProperty("soSanPhamMoi")
    public Long soSanPhamMoi = 0L;

    @JsonProperty("soLuongGiaoDich")
    public Long soLuongGiaoDich = 0L;

    @JsonProperty("tongSoDonHangThanhCong")
    public Long tongSoDonHangThanhCong = 0L;

    @JsonProperty("tongSoDonHangKhongThanhCong")
    public Long tongSoDonHangKhongThanhCong = 0L;

    @JsonProperty("tongGiaTriGiaoDich")
    public Long tongGiaTriGiaoDich = 0L;

    public void setSoNguoiBan(Long soNguoiBan) {
        this.soNguoiBan = 5 + soNguoiBan;
    }

    public void setTongSoSanPham(Long tongSoSanPham) {
        this.tongSoSanPham = 12237879 + tongSoSanPham;
    }
}

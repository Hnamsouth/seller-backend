package com.vtp.vipo.seller.services.impl;

import com.vtp.vipo.seller.common.constants.AuthConstant;
import com.vtp.vipo.seller.common.constants.BaseExceptionConstant;
import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dao.repository.MerchantRepository;
import com.vtp.vipo.seller.common.dao.repository.OrderPackageRepository;
import com.vtp.vipo.seller.common.dao.repository.ProductRepository;
import com.vtp.vipo.seller.common.dto.request.ReportRequest;
import com.vtp.vipo.seller.common.dto.response.ReportResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.services.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.utils.DateUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements ReportService {

    private final MerchantRepository merchantRepository;

    private final OrderPackageRepository orderPackageRepository;

    private final ProductRepository productRepository;

    private final PasswordEncoder encoder;

    @Value("${security.vipo.secret-key.secret-key-jwt}")
    private String jwtSecret;

    @Value("${MOIT.account}")
    private String phoneContact;

    //todo: VIPO-3903: Upload E-Contract: check out the influence of using merchant_new table here
    @Override
    public ReportResponse reportFromMOIT(ReportRequest request) {
        MerchantEntity merchantEntity = merchantRepository.findByContactPhone(request.getUserName());
        if (ObjectUtils.isEmpty(merchantEntity)) {
            throw new VipoBusinessException(BaseExceptionConstant.NOT_FOUND_ENTITY_DESCRIPTION);
        }
        if (!phoneContact.equalsIgnoreCase(request.getUserName())) {
            throw new VipoBusinessException(AuthConstant.PHONE_NOT_EXIST);
        }
        if (!encoder.matches(jwtSecret + request.getPassWord(), merchantEntity.getPassword())) {
            throw new VipoBusinessException(AuthConstant.INCORRECT_PASSWORD);
        }
        ReportResponse reportResponse = new ReportResponse();
        Long startTime = DateUtils.getTimeInSeconds(LocalDateTime.of(LocalDateTime.now().getYear(), Month.JANUARY, 1, 0, 0));
        Long endTime = DateUtils.getTimeInSeconds(LocalDateTime.now());
        log.info("Start Time: {}", startTime);
        log.info("End Time: {}", endTime);

        reportResponse.setSoLuongTruyCap(calculateAccessCount());
        reportResponse.setSoNguoiBan(merchantRepository.countActiveMerchants(null, null));
        reportResponse.setSoNguoiBanMoi(merchantRepository.countActiveMerchants(startTime, endTime));
        reportResponse.setTongSoSanPham(productRepository.countNewProducts(null, null));
        reportResponse.setSoSanPhamMoi(productRepository.countNewProducts(startTime, endTime));
        reportResponse.setTongSoDonHangThanhCong(orderPackageRepository.countOrder(Constants.PAYMENT_STATUS_SUCCESSFULL, startTime, endTime));
        reportResponse.setTongSoDonHangKhongThanhCong(orderPackageRepository.countOrder(Constants.PAYMENT_STATUS_UNSUCCESSFULL, startTime, endTime));
        reportResponse.setSoLuongGiaoDich(reportResponse.getTongSoDonHangThanhCong() + reportResponse.getTongSoDonHangKhongThanhCong());
        reportResponse.setTongGiaTriGiaoDich(orderPackageRepository.sumOrderSuccessful(startTime, endTime));
        return reportResponse;
    }

    public Long calculateAccessCount() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(Constants.START_DATE_FOR_DATE_FETCHING, now);
        long accessCount = (duration.toMinutes() / 10) + 1;
        return accessCount;
    }
}
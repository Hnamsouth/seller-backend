package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WardEntity;
import com.vtp.vipo.seller.financialstatement.common.dto.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository  extends JpaRepository<WardEntity, Long> {
    List<WardEntity> findAllByDistrictId(Long id);

    @Query(value = """
            SELECT w.name AS customerWard,
                   d.name AS customerDistrict,
                   p.name AS customerProvince
            FROM merchant m
                     LEFT JOIN  evtp_ward w on m.wardId = w.id
                     LEFT JOIN evtp_district d ON w.districtId = d.id
                     LEFT JOIN evtp_province p ON d.provinceId = p.id
            WHERE m.id = :merchantId
""", nativeQuery = true)
    Optional<CustomerAddress> findDetailByMerchantId(Long merchantId);

}

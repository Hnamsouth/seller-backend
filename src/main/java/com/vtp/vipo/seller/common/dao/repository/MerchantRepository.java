package com.vtp.vipo.seller.common.dao.repository;


import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dto.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<MerchantEntity, Long> {

    MerchantEntity findByContactPhone(String phone);

    MerchantEntity findByRefreshToken(String refreshToken);

    @Query(value = "SELECT COUNT(m.id) FROM merchant m WHERE (DATE(FROM_UNIXTIME(m.joinTime)) = (:dateNow) OR :dateNow IS NULL)", nativeQuery = true)
    Long countNumberOfSalesByTime(String dateNow);

    boolean existsByContactPhone(String contactPhone);

    @Query(value = "select m from MerchantEntity m where m.id = :id and m.inactive = :inactive " +
            "and m.status in :status")
    MerchantEntity findByContactPhoneAndInactiveAndStatus(@Param("id") Long id,
                                                          @Param("inactive") Integer inactive,
                                                          @Param("status") List<Integer> status);

    @Query(value = "select new com.vtp.vipo.seller.common.dto.UserDTO(" +
            "m.id , m.contactPhone , m.password, m.refreshToken, m.countryId," +
            "so.sellerOpenId, m.contactEmail, m.name) " +
            "FROM MerchantEntity m " +
            "LEFT JOIN SellerOpenEntity so on m.id = so.merchantId " +
            "WHERE m.id = :id")
    List<UserDTO> getUserInfo(@Param("id") Long id);

    MerchantEntity findFirstByVtpUserId(Long userId);

    @Query("SELECT count(m) FROM MerchantEntity m WHERE m.status = 1 AND " +
            "(:startTime IS NULL OR m.createTime >= :startTime) AND " +
            "(:endTime IS NULL OR m.createTime <= :endTime)")
    long countActiveMerchants(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Query(value = "select c.code as merchantCountryCode from CountryEntity c join MerchantEntity m on m.countryId = c.id where m.id = :merchantId")
    String getMerchantCountryCodeByMerchantId(Long merchantId);

    @Query(value = "select m.id from MerchantEntity m where m.status = 1 and m.inactive = 0 and m.countryId = :countryId")
    List<Long> getMerchantIds(@Param("countryId") Integer countryId);

    @Query(value = "select m.id from MerchantEntity m where m.status = 1 and m.inactive = 0")
    List<Long> getMerchantIds();
}

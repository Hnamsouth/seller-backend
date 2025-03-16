package com.vtp.vipo.seller.common.dao.repository;


import com.vtp.vipo.seller.common.dao.entity.MerchantEntity;
import com.vtp.vipo.seller.common.dao.entity.MerchantPaymentCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantPaymentCardRepository extends JpaRepository<MerchantPaymentCardEntity, Long> {
    Optional<MerchantPaymentCardEntity> findByMerchantId(Long merchantId);

    Optional<MerchantPaymentCardEntity> findFirstByMerchantIdOrderByIsDefaultDescUpdateTimeDesc(Long merchantId);

    void deleteByMerchantId(Long merchantId);
}

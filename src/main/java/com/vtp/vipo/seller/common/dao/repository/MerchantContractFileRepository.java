package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.MerchantContractFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantContractFileRepository extends JpaRepository<MerchantContractFileEntity, Long> {

    Optional<MerchantContractFileEntity> findFirstByMerchantIdAndDeletedFalseOrderByCreateTimeDesc(Long merchantId);

}

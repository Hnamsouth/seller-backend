package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.MerchantLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantLogRepository extends JpaRepository<MerchantLogEntity, Long> {

    boolean existsByMerchantIdAndAction(Long id, String name);

    Page<MerchantLogEntity> findByMerchantIdAndActionInOrderByCreateTimeDesc(
            long merchantId, List<String> action, Pageable pageable
    );

}

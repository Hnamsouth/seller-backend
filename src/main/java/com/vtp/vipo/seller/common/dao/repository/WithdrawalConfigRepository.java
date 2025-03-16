package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WithdrawalConfigRepository extends JpaRepository<WithdrawalConfigEntity, Long> {

    @Query(value = """
        select wc from WithdrawalConfigEntity wc
            join MerchantEntity m on m.merchantGroupId = wc.merchantGroupId
        where m.id = :merchantId
        """)
    Optional<WithdrawalConfigEntity> getConfigByMerchantId(Long merchantId);

}

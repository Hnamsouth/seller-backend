package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestExportEntity;
import com.vtp.vipo.seller.common.dao.entity.enums.withdrawrequest.WithdrawalRequestExportEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WithdrawalRequestExportEntityRepository extends JpaRepository<WithdrawalRequestExportEntity, Long> {

    Optional<WithdrawalRequestExportEntity> findByIdAndDeletedFalse(Long id);

    Optional<WithdrawalRequestExportEntity> findByIdAndWithdrawalRequestIdAndDeletedFalse(Long id, Long withDrawalRequestId);

    @Query("""
    select wre 
    from WithdrawalRequestExportEntity wre 
    where wre.withdrawalRequestId = :withdrawalRequestId
    and wre.status in (:statuses)
    order by wre.status, wre.createdAt desc
    """)
    Optional<WithdrawalRequestExportEntity> findFirstPendingAndSuccessReport(
            Long withdrawalRequestId, List<WithdrawalRequestExportEnum> statuses
    );
}

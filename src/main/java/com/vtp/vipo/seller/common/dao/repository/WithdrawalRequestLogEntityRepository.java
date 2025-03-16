package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.WithdrawalRequestLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRequestLogEntityRepository extends JpaRepository<WithdrawalRequestLogEntity, Long> {
    List<WithdrawalRequestLogEntity> findByWithdrawalRequestIdOrderByCreatedAtDesc(Long withdrawalRequestId);
}
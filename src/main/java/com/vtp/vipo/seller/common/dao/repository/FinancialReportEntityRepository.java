package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.FinancialReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialReportEntityRepository extends JpaRepository<FinancialReportEntity, Long> {
    List<FinancialReportEntity> findByReportIdOrderByDisplayOrder(Long reportId);

    void deleteByReportId(Long reportId);
}
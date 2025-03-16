package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ChartTopProductReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartTopProductReportEntityRepository extends JpaRepository<ChartTopProductReportEntity, Long> {
    List<ChartTopProductReportEntity> findByReportIdOrderByDisplayOrder(Long reportId);

    void deleteByReportId(Long reportId);
}
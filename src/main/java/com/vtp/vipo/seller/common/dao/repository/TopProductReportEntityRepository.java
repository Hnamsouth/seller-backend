package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.TopProductReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopProductReportEntityRepository extends JpaRepository<TopProductReportEntity, Long> {
    List<TopProductReportEntity> findByReportIdOrderByRanking(Long reportId);

    void deleteByReportId(Long reportId);
}
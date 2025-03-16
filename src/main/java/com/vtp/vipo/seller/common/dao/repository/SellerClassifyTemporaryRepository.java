package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.SellerAttributeTemporaryEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerClassifyEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerClassifyTemporaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerClassifyTemporaryRepository extends JpaRepository<SellerClassifyTemporaryEntity, Long> {
    void deleteByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
    List<SellerClassifyTemporaryEntity> findAllByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
}


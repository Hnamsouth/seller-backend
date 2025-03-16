package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.SellerAttributeEntity;
import com.vtp.vipo.seller.common.dao.entity.SellerAttributeTemporaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerAttributeTemporaryRepository extends JpaRepository<SellerAttributeTemporaryEntity, Long> {
    void deleteByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
    List<SellerAttributeTemporaryEntity> findAllByProductTemporaryId(@Param("productTemporaryId") Long productTemporaryId);
}


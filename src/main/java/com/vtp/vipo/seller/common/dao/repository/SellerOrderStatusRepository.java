package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.SellerOrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerOrderStatusRepository extends JpaRepository<SellerOrderStatusEntity, Long> {

    List<SellerOrderStatusEntity> findAllByParentId(Long parentId);

}


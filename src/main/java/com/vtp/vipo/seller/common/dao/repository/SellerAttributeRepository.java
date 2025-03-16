package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.SellerAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerAttributeRepository extends JpaRepository<SellerAttributeEntity, Long> {
    void deleteByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT new SellerAttributeEntity(s.id,s.productId,sl.attributeName,s.attributeOrder,s.createdDate,s.updatedDate) from SellerAttributeEntity s " +
            "left join SellerAttributeLanguage sl on s.id = sl.sellerAttributeId and sl.language = :language " +
            "where s.productId = :productId ")
    List<SellerAttributeEntity> findAllByProductId(@Param("productId") Long productId,
                                                   @Param("language") String language);

    List<SellerAttributeEntity> findAllByProductIdAndDeletedFalse(Long productId);

}


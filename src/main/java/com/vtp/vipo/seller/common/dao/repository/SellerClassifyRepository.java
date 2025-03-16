package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.SellerClassifyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SellerClassifyRepository extends JpaRepository<SellerClassifyEntity, Long> {
    void deleteByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT new SellerClassifyEntity(s.id,s.productId,s.sellerAttributeId,s.sellerImage," +
            "sl.classifyName,s.orderClassify,s.createdDate,s.updatedDate) from SellerClassifyEntity s " +
            "left join SellerClassifyLanguage sl on s.id = sl.sellerClassifyId and sl.language = :language " +
            "where s.productId = :productId ")
    List<SellerClassifyEntity> findAllByProductId(@Param("productId") Long productId,
                                                  @Param("language") String language);

    List<SellerClassifyEntity> findAllByProductIdAndDeletedFalse(Long productId);
}


package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.ProductCertificateEntity;
import com.vtp.vipo.seller.common.dto.response.base.PagingRs;
import com.vtp.vipo.seller.common.enumseller.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductCertificateEntityRepository extends JpaRepository<ProductCertificateEntity, Long> {

    Optional<ProductCertificateEntity> findByIdAndDeletedFalse(Long id);

    Optional<ProductCertificateEntity> findByIdAndProductIdAndDeletedFalse(Long id, Long productId);

    Page<ProductCertificateEntity> findByProductIdAndDeletedFalse(Long productId, Pageable pageable);

    List<ProductCertificateEntity> findByProductIdAndDeletedFalse(Long productId);

    @Query("SELECT pc FROM ProductCertificateEntity pc WHERE pc.productId = :productId AND pc.deleted = false ORDER BY pc.createdAt DESC")
    List<ProductCertificateEntity> findCertificatesByProduct(Long productId);

    List<ProductCertificateEntity> findByProductId(Long productId);

    List<ProductCertificateEntity> findByProductTempId(Long productTemporaryId);

    @Query("SELECT p FROM ProductCertificateEntity p " +
            "WHERE (" +
            "       (p.productId IS NULL AND p.productTempId IS NULL) " +
            "       OR p.deleted = TRUE " +
            ") " +
            "AND p.createdAt < :createdAt"
    )
    List<ProductCertificateEntity> findOldDeletedCertificates(@Param("createdAt") LocalDateTime createdAt);

}

package com.vtp.vipo.seller.common.dao.repository;

import com.vtp.vipo.seller.common.dao.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Query("SELECT new CategoryEntity(c.id, c.parentId, " +
            "COALESCE(cl.name, c.name), c.icon, c.languages) " +
            "FROM CategoryEntity c " +
            "LEFT JOIN FETCH CategoryLanguageEntity cl ON c.id = cl.categoryId AND cl.language = :language " +
            "ORDER BY c.id ASC ")
    List<CategoryEntity> fetchCategoriesWithLanguage(@Param("language") String language);
    @Query("SELECT c FROM CategoryEntity c WHERE c.code IN :code")
    List<CategoryEntity> findAllByCode(@Param("code") List<String> code);

}

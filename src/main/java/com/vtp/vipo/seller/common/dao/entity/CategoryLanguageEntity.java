package com.vtp.vipo.seller.common.dao.entity;

import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "category_language")
@Getter
@Setter
@Data
@AllArgsConstructor
public class CategoryLanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "categoryId", nullable = false)
    private Long categoryId;

    @Column(name = "language", nullable = false, length = 3)
    private String language;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    // Constructors
    public CategoryLanguageEntity() {
    }

    public CategoryLanguageEntity(Long categoryId, String language, String name) {
        this.categoryId = categoryId;
        this.language = language;
        this.name = name;
    }
}

package com.vtp.vipo.seller.common.dao.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base class for all entities in the application. An entity represents a single result row (record)
 * retrieved from the database and provides methods for retrieving and storing properties associated
 * with the row (record). It uses the @MappedSuperclass annotation to indicate that it is a base
 * class for entities. The class also uses the @EntityListeners annotation to register the
 * AuditingEntityListener for auditing fields. The class implements Serializable, which means it can
 * be converted to a byte stream and restored from it.
 *
 * @author haidv
 * @version 1.0
 */
@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    /**
     * The serialVersionUID is a universal version identifier for a Serializable class.
     * Deserialization uses this number to ensure that a loaded class corresponds exactly to a
     * serialized object. If no match is found, then an InvalidClassException is thrown.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the entity. It uses the @Id annotation to indicate that it is the
     * primary key of the entity. The @GeneratedValue and @GenericGenerator annotations are used to
     * specify the generation strategy for the primary key. The value of the primary key is generated
     * using the SnowflakeGeneratorStrategy.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", type = SnowflakeGeneratorStrategy.class)
    private Long id;

    /**
     * The date and time when the entity was created. It is automatically set by the application when
     * the entity is created. It uses the @CreatedDate annotation to indicate that it is automatically
     * managed by the Spring Data JPA auditing mechanism.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * The date and time when the entity was last updated. It is automatically set by the application
     * whenever the entity is updated. It uses the @LastModifiedDate annotation to indicate that it is
     * automatically managed by the Spring Data JPA auditing mechanism.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * The user who created the entity. It is automatically set by the application when the entity is
     * created. It uses the @CreatedBy annotation to indicate that it is automatically managed by the
     * Spring Data JPA auditing mechanism.
     */
    @CreatedBy
    private String createdBy;

    /**
     * The user who last updated the entity. It is automatically set by the application whenever the
     * entity is updated. It uses the @LastModifiedBy annotation to indicate that it is automatically
     * managed by the Spring Data JPA auditing mechanism.
     */
    @LastModifiedBy
    private String updatedBy;

    /**
     * A flag indicating whether the entity has been deleted. It is not physically deleted from the
     * database, but is marked as deleted with this flag.
     */
    @Column(name = "isDeleted")
    private boolean deleted = false;

    /**
     * The version of the entity. It is used for optimistic locking. If two users retrieve the same
     * row, make a change, and then save the change, the last user's update would overwrite the first
     * user's update without this version field. The @Version annotation is used to indicate that it
     * is a version field.
     */
    @Version
    @Column(name = "VERSION")
    private Long version;
}


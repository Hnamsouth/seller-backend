package com.vtp.vipo.seller.common.dao.entity.base.v2;

import com.vtp.vipo.seller.common.dao.entity.base.SnowflakeGeneratorStrategy;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntityWithSnowflakeIdAndEpochSecondsTime extends BaseEntityWithEpochSecondsTime {

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

}

package com.vtp.vipo.seller.common.dao.entity;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import com.vtp.vipo.seller.common.dto.ActivityDetailsData;
import com.vtp.vipo.seller.common.dto.ActivityDetailsMetadata;
import com.vtp.vipo.seller.common.enumseller.ActivityType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Type;

@ToString
@Table(name = "order_activity_history")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderActivityHistoryEntity extends BaseEntity {
    Long orderId;

    @Enumerated(EnumType.STRING)
    ActivityType type;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    ActivityDetailsData details;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    Object beforeState;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    Object afterState;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    ActivityDetailsMetadata metadata;
}

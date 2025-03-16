package com.vtp.vipo.seller.retryservice.dao.entity;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import com.vtp.vipo.seller.common.dao.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * @author haidv
 * @version 1.0
 */
@Entity
@Table(name = "tbl_retries_message")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetriesMessage extends BaseEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tbl_retries_message_id_seq")
    @SequenceGenerator(
            name = "tbl_retries_message_id_seq",
            sequenceName = "tbl_retries_message_id_seq",
            allocationSize = 1)
    private Long id;

    private String messageId;

    private String data;

    private String source;

    private int retriesNo;

    private String topic;

    private String destination;

    private String deadLetterQueue;

    private Long delayTime;

    private Integer repeatCount;

    private LocalDateTime nextExecuteAt;
}

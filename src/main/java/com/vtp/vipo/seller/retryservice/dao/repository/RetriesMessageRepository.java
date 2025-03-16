package com.vtp.vipo.seller.retryservice.dao.repository;

import com.vtp.vipo.seller.config.mq.kafka.RetriesMessageData;
import com.vtp.vipo.seller.retryservice.dao.entity.RetriesMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface RetriesMessageRepository extends JpaRepository<RetriesMessage, Long> {
    Optional<RetriesMessage> findByMessageId(String retryMessageId);

    @Modifying
    void deleteByMessageId(String messageId);


    @Query("""
                select m 
                from RetriesMessage m 
                where 
                    m.retriesNo <= m.repeatCount 
                    and m.nextExecuteAt <= :currentLocalDateTime
                order by m.retriesNo
                """)
    List<RetriesMessage> findByRetriesActivated(LocalDateTime currentLocalDateTime);
}
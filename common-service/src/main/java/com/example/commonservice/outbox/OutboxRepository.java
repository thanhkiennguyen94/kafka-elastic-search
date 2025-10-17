package com.example.commonservice.outbox;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop50ByStatusOrderOrderByIdAsc(OutboxStatus status);
    List<OutboxEvent> findTop50ByStatusElasticOrderByIdAsc(OutboxStatus status);
    @Transactional
    void deleteByStatusOrderAndStatusElasticAndCreatedAtBefore(OutboxStatus statusOrder,OutboxStatus statusElastic, LocalDateTime dateTime);
}
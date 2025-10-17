package com.example.commonservice.outbox;
import com.example.commonservice.event.CreateUserEvent;
import com.example.commonservice.util.ConstantEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRY = 3;

    /**
     * Lưu sự kiện vào bảng outbox, để commit cùng transaction với entity chính
     */
    @Transactional
    public void saveEvent(String aggregateType, Long aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .statusOrder(OutboxStatus.PENDING)
                    .build();
            outboxRepository.save(event);
            log.info("🟡 Outbox event saved: {} - {}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("❌ Failed to save outbox event: {}", e.getMessage(), e);
        }
    }


    // Chạy theo schedule
    @Scheduled(fixedDelay = 5000)
    public void processOrderOutbox() {
        List<OutboxEvent> events = outboxRepository.findTop50ByStatusOrderOrderByIdAsc(OutboxStatus.PENDING);
        for (OutboxEvent event : events) {
            processSingleEvent(event);
        }
    }

    // Xử lý 1 event riêng
    public void processSingleEvent(OutboxEvent event) {
        try {
            Object payloadObj = null;
            if (ConstantEventType.EVENT_CREATE_USER.equals(event.getEventType())) {
                payloadObj = objectMapper.readValue(event.getPayload(), CreateUserEvent.class);
            }
            // TODO: xử lý các event khác nếu có

            if (payloadObj != null) {
                String topic = event.getAggregateType().toLowerCase() + "-created-topic";
                kafkaTemplate.send(topic, payloadObj);

                event.setStatusOrder(OutboxStatus.SENT);
                event.setLastError(null);
                outboxRepository.save(event);

                log.info("✅ Sent Order OutboxEvent id={} to topic={}", event.getId(), topic);
            }
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(ex.getMessage());
            if (event.getRetryCount() >= MAX_RETRY) {
                event.setStatusOrder(OutboxStatus.FAILED);
            }
            outboxRepository.save(event);
        }
    }

    // Chạy mỗi ngày lúc 2h sáng
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanUpSentEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        outboxRepository.deleteByStatusOrderAndStatusElasticAndCreatedAtBefore(OutboxStatus.SENT,OutboxStatus.SENT, cutoff);
        log.info("🧹 Deleted SENT outbox events before {}", cutoff);
    }
}
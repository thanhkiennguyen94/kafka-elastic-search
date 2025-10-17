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

    /**
     * 🕐 Chạy mỗi 5s để gửi event sang Kafka
     */
    @Scheduled(fixedDelay = 5000)
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findTop50ByStatusOrderOrderByIdAsc(OutboxStatus.PENDING);
        if (events.isEmpty()) return;

        for (OutboxEvent event : events) {
            try {
                String topic = event.getAggregateType().toLowerCase() + "-created-topic";

                // Deserialize payload thành object tương ứng
                Object payloadObj;
                if (event.getEventType().equals(ConstantEventType.EVENT_CREATE_USER)) {
                    payloadObj = objectMapper.readValue(event.getPayload(), CreateUserEvent.class);
                    // TODO: các event khác
                } else {
                    payloadObj = event.getPayload(); // fallback string
                }

                // Gửi object sang Kafka
                kafkaTemplate.send(topic, payloadObj);

                event.setStatusOrder(OutboxStatus.SENT);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(null);
                outboxRepository.save(event);

                log.info("✅ Sent OutboxEvent id={} to topic={}", event.getId(), topic);

            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setLastError(ex.getMessage());

                if (event.getRetryCount() >= MAX_RETRY) {
                    event.setStatusOrder(OutboxStatus.FAILED);
                    log.error("❌ Failed after {} retries: id={} error={}", MAX_RETRY, event.getId(), ex.getMessage());
                } else {
                    log.warn("⚠️ Retry {}/{} for event id={}", event.getRetryCount(), MAX_RETRY, event.getId());
                }
                outboxRepository.save(event);
            }
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
package com.example.notificationservice;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final ElasticsearchEventService elasticsearchEventService;

    public KafkaEventConsumer(ElasticsearchEventService elasticsearchEventService) {
        this.elasticsearchEventService = elasticsearchEventService;
    }

    @KafkaListener(topics = {"user.events", "order.events"})
    public void consume(ConsumerRecord<String, String> record) {
        String eventType = record.topic().equals("user.events") ? "USER" : "ORDER";
        logger.info("Received {} event:\n{}", eventType, record.value());
        elasticsearchEventService.save(record.topic(), record.value());
    }
}

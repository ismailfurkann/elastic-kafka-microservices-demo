package com.example.orderservice;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200", methods = org.springframework.web.bind.annotation.RequestMethod.POST, allowedHeaders = "Content-Type")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final String ORDER_EVENTS_TOPIC = "order.events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderController(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderEvent createOrder(@RequestBody OrderRequest request) {
        OrderEvent event = new OrderEvent("ORDER_CREATED", request.product(), request.price());
        kafkaTemplate.send(ORDER_EVENTS_TOPIC, event);
        return event;
    }
}

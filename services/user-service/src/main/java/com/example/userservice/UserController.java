package com.example.userservice;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:4200", methods = org.springframework.web.bind.annotation.RequestMethod.POST, allowedHeaders = "Content-Type")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final String USER_EVENTS_TOPIC = "user.events";

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserController(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEvent createUser(@RequestBody UserRequest request) {
        UserEvent event = new UserEvent("USER_CREATED", request.name(), request.email());
        kafkaTemplate.send(USER_EVENTS_TOPIC, event);
        return event;
    }
}

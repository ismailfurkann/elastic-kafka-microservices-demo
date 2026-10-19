package com.example.notificationservice;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchEventService {

    private static final String INDEX_NAME = "application-events";

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    public ElasticsearchEventService(ElasticsearchClient elasticsearchClient, ObjectMapper objectMapper) {
        this.elasticsearchClient = elasticsearchClient;
        this.objectMapper = objectMapper;
    }

    public void save(String sourceTopic, String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("eventType", event.path("type").asText());
            document.put("sourceTopic", sourceTopic);
            document.put("payload", payload);
            document.put("timestamp", Instant.now().toString());

            elasticsearchClient.index(request -> request
                    .index(INDEX_NAME)
                    .document(document));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store event in Elasticsearch", exception);
        }
    }
}

package com.pharmastock.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OpenAIProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "gpt-3.5-turbo";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "OpenAI API key belum dikonfigurasi. Silakan atur di file .env.";
        }

        try {
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 1024);
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");

            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());
                return json.path("choices").path(0).path("message").path("content").asText();
            } else {
                return "Error dari OpenAI API (HTTP " + response.statusCode() + "): " + response.body();
            }
        } catch (Exception e) {
            return "Gagal menghubungi OpenAI: " + e.getMessage();
        }
    }
}

package com.kepo.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private static final String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    public GeminiProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "gemini-2.5-flash-lite";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[Gemini] API key tidak dikonfigurasi");
            return "Gemini API key belum dikonfigurasi. Silakan atur di file .env dengan variabel AI_GEMINI_API_KEY.";
        }

        try {
            String url = API_BASE + model + ":generateContent?key=" + apiKey;
            System.err.println("[Gemini] Mengirim request ke model: " + model);

            ObjectNode requestBody = mapper.createObjectNode();

            // System instruction
            ObjectNode systemInstruction = mapper.createObjectNode();
            ArrayNode sysParts = systemInstruction.putArray("parts");
            ObjectNode sysPart = sysParts.addObject();
            sysPart.put("text", systemPrompt);
            requestBody.set("systemInstruction", systemInstruction);

            // User content
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode content = contents.addObject();
            content.put("role", "user");
            ArrayNode parts = content.putArray("parts");
            ObjectNode part = parts.addObject();
            part.put("text", userMessage);

            // Generation config
            ObjectNode genConfig = mapper.createObjectNode();
            genConfig.put("temperature", 0.3); // Low temperature for high factual accuracy in RAG
            genConfig.put("maxOutputTokens", 2048);
            requestBody.set("generationConfig", genConfig);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json = mapper.readTree(response.body());

                JsonNode candidates = json.path("candidates");
                if (candidates.isArray() && candidates.isEmpty()) {
                    return "Respons diblokir oleh filter keamanan Gemini. Silakan coba pertanyaan lain.";
                }

                JsonNode text = json.path("candidates").path(0)
                        .path("content").path("parts").path(0)
                        .path("text");

                if (text.isMissingNode() || text.isNull()) {
                    return "Tidak ada respons dari Gemini. Silakan coba lagi.";
                }

                return text.asText();
            } else {
                JsonNode errorJson = null;
                try {
                    errorJson = mapper.readTree(response.body());
                } catch (Exception ignored) {}

                String errorMsg = errorJson != null && errorJson.has("error")
                        ? errorJson.path("error").path("message").asText()
                        : response.body();

                return "Error dari Gemini API (HTTP " + response.statusCode() + "): " + errorMsg;
            }
        } catch (Exception e) {
            return "Gagal menghubungi Gemini: " + e.getMessage();
        }
    }
}

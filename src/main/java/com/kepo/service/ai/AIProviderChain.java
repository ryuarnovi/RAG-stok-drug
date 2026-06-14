package com.kepo.service.ai;

public class AIProviderChain implements AIProvider {

    private final AIProvider primary;
    private final AIProvider fallback;

    public AIProviderChain(AIProvider primary, AIProvider fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        String response = primary.chat(systemPrompt, userMessage);
        if (response == null || isError(response)) {
            System.err.println("[AIChain] Primary provider failed, falling back to local provider. Primary output: " + response);
            return fallback.chat(systemPrompt, userMessage);
        }
        return response;
    }

    private boolean isError(String response) {
        String lower = response.toLowerCase();
        return lower.contains("error") ||
               lower.contains("gagal") ||
               lower.contains("belum dikonfigurasi") ||
               lower.contains("http ") ||
               lower.contains("tidak ada respons") ||
               lower.contains("diblokir") ||
               lower.contains("expired") ||
               lower.contains("api key");
    }
}

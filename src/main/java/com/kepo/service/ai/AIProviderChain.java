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
        String lower = response.toLowerCase().trim();
        // Only treat as error if response starts with these patterns (not mid-sentence)
        return lower.startsWith("error") ||
               lower.startsWith("gagal") ||
               lower.startsWith("belum dikonfigurasi") ||
               lower.startsWith("tidak ada respons") ||
               lower.startsWith("diblokir") ||
               lower.startsWith("expired") ||
               lower.matches("^https?://.*") ||
               lower.startsWith("api key");
    }
}

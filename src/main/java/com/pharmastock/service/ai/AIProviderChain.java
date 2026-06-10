package com.pharmastock.service.ai;

/**
 * AI Provider dengan fallback mechanism.
 * Mencoba provider utama terlebih dahulu, jika gagal fallback ke provider lain.
 */
public class AIProviderChain implements AIProvider {

    private final AIProvider primary;
    private final AIProvider fallback;

    public AIProviderChain(AIProvider primary, AIProvider fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        String result = primary.chat(systemPrompt, userMessage);

        // Jika hasil mengandung error, coba fallback
        if (result != null && isError(result)) {
            System.err.println("[AI] Primary provider gagal: " + result);
            System.err.println("[AI] Mencoba fallback provider...");
            String fallbackResult = fallback.chat(systemPrompt, userMessage);
            return fallbackResult;
        }

        return result;
    }

    private boolean isError(String response) {
        if (response == null) return true;
        String lower = response.toLowerCase();
        return lower.contains("error") ||
               lower.contains("gagal") ||
               lower.contains("belum dikonfigurasi") ||
               lower.contains("http ") ||
               lower.contains("tidak ada respons") ||
               lower.contains("diblokir") ||
               lower.contains("invalid") ||
               lower.contains("unauthorized") ||
               lower.contains("forbidden") ||
               lower.contains("not found") ||
               lower.contains("rate limit");
    }
}

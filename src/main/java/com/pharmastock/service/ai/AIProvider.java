package com.pharmastock.service.ai;

public interface AIProvider {
    String chat(String systemPrompt, String userMessage);
}

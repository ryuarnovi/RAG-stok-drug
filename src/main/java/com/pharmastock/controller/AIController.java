package com.pharmastock.controller;

import com.pharmastock.model.AIRecommendation;
import com.pharmastock.service.AIRecommendationService;

import java.util.List;

public class AIController {

    private final AIRecommendationService aiService;

    public AIController(AIRecommendationService aiService) {
        this.aiService = aiService;
    }

    public String sendMessage(String message) {
        return aiService.chat(message);
    }

    public List<AIRecommendation> getLowStockRecommendations() {
        return aiService.getLowStockRecommendations();
    }

    public List<AIRecommendation> getExpiryRecommendations() {
        return aiService.getExpiryRecommendations();
    }

    public List<AIRecommendation> getSmartInsights() {
        return aiService.getSmartInsights();
    }

    public AIRecommendation getDemandForecast(int medicineId) {
        return aiService.getDemandForecast(medicineId);
    }

    public List<AIRecommendation> getAllPredictions() {
        return aiService.getAIPredictions();
    }

    public String[] getSuggestions() {
        return new String[]{
                "Berapa sisa Paracetamol?",
                "Cek obat kadaluwarsa",
                "Stok kritis hari ini",
                "Rekomendasi reorder",
                "Total inventaris"
        };
    }
}

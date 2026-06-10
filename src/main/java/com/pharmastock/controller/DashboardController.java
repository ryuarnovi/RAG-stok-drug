package com.pharmastock.controller;

import com.pharmastock.model.AIRecommendation;
import com.pharmastock.model.InventoryTransaction;
import com.pharmastock.service.AIRecommendationService;
import com.pharmastock.service.InventoryService;

import java.util.List;

public class DashboardController {

    private final InventoryService inventoryService;
    private final AIRecommendationService aiService;

    public DashboardController(InventoryService inventoryService, AIRecommendationService aiService) {
        this.inventoryService = inventoryService;
        this.aiService = aiService;
    }

    public InventoryService.DashboardStats getDashboardStats() {
        return inventoryService.getDashboardStats();
    }

    public List<InventoryTransaction> getRecentActivity(int limit) {
        return inventoryService.getRecentActivity(limit);
    }

    public List<AIRecommendation> getAIPredictions() {
        return aiService.getAIPredictions();
    }

    public AIRecommendation getTopPrediction() {
        List<AIRecommendation> predictions = aiService.getAIPredictions();
        return predictions.stream()
                .filter(AIRecommendation::isCritical)
                .findFirst()
                .orElse(predictions.isEmpty() ? null : predictions.get(0));
    }
}

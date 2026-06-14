package com.kepo.controller;

import com.kepo.model.AuditLog;
import com.kepo.model.Shelter;
import com.kepo.service.*;

import java.util.List;

public class DashboardController {

    private final ShelterService shelterService;
    private final RefugeeService refugeeService;
    private final InventoryService inventoryService;
    private final DistributionService distributionService;
    private final EventService eventService;
    private final AIRecommendationService aiRecService;
    private final UserService userService;

    public DashboardController(ShelterService shelterService, RefugeeService refugeeService,
                               InventoryService inventoryService, DistributionService distributionService,
                               EventService eventService, AIRecommendationService aiRecService,
                               UserService userService) {
        this.shelterService = shelterService;
        this.refugeeService = refugeeService;
        this.inventoryService = inventoryService;
        this.distributionService = distributionService;
        this.eventService = eventService;
        this.aiRecService = aiRecService;
        this.userService = userService;
    }

    public int getActiveEventsCount() {
        return (int) eventService.getAllEvents().stream().filter(e -> "ACTIVE".equals(e.getStatus())).count();
    }

    public int getTotalSheltersCount() {
        return shelterService.getAllShelters().size();
    }

    public int getCriticalSheltersCount() {
        return (int) shelterService.getAllShelters().stream()
                .filter(s -> "KRITIS".equals(s.getStatus()) || s.getCurrentOccupancy() >= s.getCapacity()).count();
    }

    public int getTotalRefugeesCount() {
        return (int) refugeeService.getAllRefugees().stream().filter(r -> "CHECKED_IN".equals(r.getStatus())).count();
    }

    public int getLowStockMedicinesCount() {
        return inventoryService.getLowStockMedicines().size();
    }

    public int getTodayDistributionsCount() {
        return distributionService.getAllDistributions().size();
    }

    public List<com.kepo.model.Distribution> getDistributions() {
        return distributionService.getAllDistributions();
    }

    public List<String> getEmergencyAlerts() {
        return aiRecService.getEmergencyAlerts();
    }

    public List<String> getAISuggestions() {
        return aiRecService.getAISuggestions();
    }

    public List<AuditLog> getRecentActivities() {
        List<AuditLog> logs = userService.getAuditLogs();
        if (logs.size() > 5) {
            return logs.subList(0, 5);
        }
        return logs;
    }

    public List<Shelter> getShelters() {
        return shelterService.getAllShelters();
    }
}

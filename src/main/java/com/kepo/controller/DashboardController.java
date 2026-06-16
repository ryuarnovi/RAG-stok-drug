package com.kepo.controller;

import com.kepo.model.AuditLog;
import com.kepo.model.Shelter;
import com.kepo.model.ShelterStock;
import com.kepo.service.*;

import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    private final ShelterService shelterService;
    private final RefugeeService refugeeService;
    private final InventoryService inventoryService;
    private final DistributionService distributionService;
    private final EventService eventService;
    private final AIRecommendationService aiRecService;
    private final UserService userService;
    private final ShelterStockService shelterStockService;

    public DashboardController(ShelterService shelterService, RefugeeService refugeeService,
                               InventoryService inventoryService, DistributionService distributionService,
                               EventService eventService, AIRecommendationService aiRecService,
                               UserService userService, ShelterStockService shelterStockService) {
        this.shelterService = shelterService;
        this.refugeeService = refugeeService;
        this.inventoryService = inventoryService;
        this.distributionService = distributionService;
        this.eventService = eventService;
        this.aiRecService = aiRecService;
        this.userService = userService;
        this.shelterStockService = shelterStockService;
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

    public int getFullSheltersCount() {
        return (int) shelterService.getAllShelters().stream()
                .filter(s -> s.getCurrentOccupancy() >= s.getCapacity() && s.getCapacity() > 0)
                .count();
    }

    public int getAvailableSheltersCount() {
        return (int) shelterService.getAllShelters().stream()
                .filter(s -> s.getCurrentOccupancy() < s.getCapacity())
                .count();
    }

    public int getRefugeePriorityCount(String priority) {
        return (int) refugeeService.getAllRefugees().stream()
                .filter(r -> "CHECKED_IN".equals(r.getStatus()) && priority.equals(r.getPriorityStatus()))
                .count();
    }

    public int getCriticalLogisticsSheltersCount() {
        int count = 0;
        for (Shelter s : shelterService.getAllShelters()) {
            List<ShelterStock> stocks = shelterStockService.getShelterStocks(s.getShelterId());
            boolean hasCritical = false;
            for (ShelterStock stock : stocks) {
                if (shelterStockService.calculateAvailabilityPercentage(stock) < 50.0) {
                    hasCritical = true;
                    break;
                }
            }
            if (hasCritical && !stocks.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    public List<Shelter> getTopLogisticNeedyShelters() {
        List<Shelter> all = shelterService.getAllShelters();
        // Sort by average availability percentage ascending
        all.sort((s1, s2) -> {
            double avg1 = getAverageAvailability(s1.getShelterId());
            double avg2 = getAverageAvailability(s2.getShelterId());
            return Double.compare(avg1, avg2);
        });
        if (all.size() > 5) {
            return all.subList(0, 5);
        }
        return all;
    }

    public double getAverageAvailability(int shelterId) {
        List<ShelterStock> stocks = shelterStockService.getShelterStocks(shelterId);
        if (stocks.isEmpty()) return 100.0;
        double sum = 0;
        for (ShelterStock s : stocks) {
            sum += shelterStockService.calculateAvailabilityPercentage(s);
        }
        return sum / stocks.size();
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

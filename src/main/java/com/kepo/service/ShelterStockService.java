package com.kepo.service;

import com.kepo.model.Shelter;
import com.kepo.model.ShelterStock;
import com.kepo.repository.ShelterStockRepository;
import com.kepo.repository.ShelterRepository;

import java.util.List;

public class ShelterStockService {
    private final ShelterStockRepository shelterStockRepo;
    private final ShelterRepository shelterRepo;

    public ShelterStockService(ShelterStockRepository shelterStockRepo, ShelterRepository shelterRepo) {
        this.shelterStockRepo = shelterStockRepo;
        this.shelterRepo = shelterRepo;
    }

    public List<ShelterStock> getShelterStocks(int shelterId) {
        return shelterStockRepo.findByShelterId(shelterId);
    }

    public List<ShelterStock> getAllCriticalStocks() {
        return shelterStockRepo.findAllCritical();
    }

    public boolean updateShelterStock(int shelterId, int medicineId, int qtyChange) {
        return shelterStockRepo.updateStock(shelterId, medicineId, qtyChange);
    }

    public boolean saveShelterStock(ShelterStock stock) {
        return shelterStockRepo.save(stock);
    }

    /**
     * Calculates the availability percentage of a specific medicine at a shelter
     * using the Hybrid Approach (Days of Coverage for active phase, Minimum Stock for standby).
     */
    public double calculateAvailabilityPercentage(ShelterStock stock) {
        Shelter shelter = shelterRepo.findById(stock.getShelterId());
        if (shelter == null) {
            return 0.0;
        }

        int occupancy = shelter.getCurrentOccupancy();
        if (occupancy <= 0) {
            // Standby/Prep Phase - Use Approach A (Minimum Stock)
            if (stock.getMinimumStock() <= 0) {
                return stock.getQuantity() > 0 ? 100.0 : 0.0;
            }
            double percentage = ((double) stock.getQuantity() / stock.getMinimumStock()) * 100.0;
            return Math.min(100.0, percentage);
        } else {
            // Active Phase - Use Approach C (Days of Coverage)
            double dailyConsumptionFactor = getDailyConsumptionFactor(stock.getMedicineCode());
            double dailyDemand = occupancy * dailyConsumptionFactor;
            
            if (dailyDemand <= 0) {
                return 100.0;
            }

            double daysOfCoverage = stock.getQuantity() / dailyDemand;
            double targetDays = 7.0; // Target coverage is 7 days

            double percentage = (daysOfCoverage / targetDays) * 100.0;
            return Math.min(100.0, percentage);
        }
    }

    /**
     * Estimates days of coverage remaining.
     */
    public double estimateDaysOfCoverage(ShelterStock stock, int occupancy) {
        if (occupancy <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        double dailyConsumptionFactor = getDailyConsumptionFactor(stock.getMedicineCode());
        double dailyDemand = occupancy * dailyConsumptionFactor;
        if (dailyDemand <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return stock.getQuantity() / dailyDemand;
    }

    /**
     * Renders standard daily consumption factors per refugee based on standard disaster logistics guidelines.
     */
    private double getDailyConsumptionFactor(String medCode) {
        if (medCode == null) return 0.2;
        return switch (medCode) {
            case "MED-001" -> 0.3; // Amoxicillin (30% of refugees need daily dose on average)
            case "MED-002" -> 0.5; // Paracetamol (high demand analgesics/fever reducer)
            case "MED-004" -> 0.4; // Ibuprofen
            case "MED-003", "MED-007" -> 0.25; // Antihistamine/cough syrups
            case "MED-008" -> 0.6; // Vitamin C (daily supplement)
            case "MED-009", "MED-005", "MED-010" -> 0.2; // Omeprazole, Metformin, Amlodipine
            default -> 0.2;
        };
    }
}

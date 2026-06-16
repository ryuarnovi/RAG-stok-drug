package com.kepo.controller;

import com.kepo.model.RefugeeMovement;
import com.kepo.model.ShelterStock;
import com.kepo.service.RefugeeShelterService;
import com.kepo.service.ShelterStockService;
import com.kepo.service.UserService;

import java.util.List;

public class RefugeeShelterController {
    private final RefugeeShelterService refugeeShelterService;
    private final ShelterStockService shelterStockService;
    private final UserService userService;

    public RefugeeShelterController(RefugeeShelterService refugeeShelterService,
                                     ShelterStockService shelterStockService,
                                     UserService userService) {
        this.refugeeShelterService = refugeeShelterService;
        this.shelterStockService = shelterStockService;
        this.userService = userService;
    }

    public boolean transferRefugee(int refugeeId, Integer targetShelterId, String notes) {
        String operator = "system";
        if (userService.getCurrentUser() != null) {
            operator = userService.getCurrentUser().getUsername();
        }
        return refugeeShelterService.transferRefugee(refugeeId, targetShelterId, operator, notes);
    }

    public List<RefugeeMovement> getMovementHistory(int refugeeId) {
        return refugeeShelterService.getMovementHistory(refugeeId);
    }

    public List<RefugeeMovement> getAllMovements() {
        return refugeeShelterService.getAllMovements();
    }

    public List<ShelterStock> getShelterStocks(int shelterId) {
        return shelterStockService.getShelterStocks(shelterId);
    }

    public List<ShelterStock> getAllCriticalStocks() {
        return shelterStockService.getAllCriticalStocks();
    }

    public boolean updateShelterStock(int shelterId, int medicineId, int qtyChange) {
        return shelterStockService.updateShelterStock(shelterId, medicineId, qtyChange);
    }

    public boolean saveShelterStock(ShelterStock stock) {
        return shelterStockService.saveShelterStock(stock);
    }

    public double calculateAvailabilityPercentage(ShelterStock stock) {
        return shelterStockService.calculateAvailabilityPercentage(stock);
    }

    public double estimateDaysOfCoverage(ShelterStock stock, int occupancy) {
        return shelterStockService.estimateDaysOfCoverage(stock, occupancy);
    }
}

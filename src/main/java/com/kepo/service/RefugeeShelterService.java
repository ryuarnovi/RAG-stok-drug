package com.kepo.service;

import com.kepo.model.Refugee;
import com.kepo.model.RefugeeMovement;
import com.kepo.repository.RefugeeRepository;
import com.kepo.repository.RefugeeMovementRepository;
import com.kepo.repository.ShelterRepository;

import java.util.List;

public class RefugeeShelterService {
    private final RefugeeRepository refugeeRepo;
    private final RefugeeMovementRepository movementRepo;
    private final ShelterRepository shelterRepo;

    public RefugeeShelterService(RefugeeRepository refugeeRepo, RefugeeMovementRepository movementRepo, ShelterRepository shelterRepo) {
        this.refugeeRepo = refugeeRepo;
        this.movementRepo = movementRepo;
        this.shelterRepo = shelterRepo;
    }

    public boolean transferRefugee(int refugeeId, Integer targetShelterId, String operatorUsername, String notes) {
        Refugee refugee = refugeeRepo.findById(refugeeId);
        if (refugee == null) {
            return false;
        }

        Integer oldShelterId = refugee.getShelterId();
        if (targetShelterId != null && targetShelterId.equals(oldShelterId)) {
            return true; // No shelter change
        }

        refugee.setShelterId(targetShelterId);
        if (targetShelterId != null) {
            refugee.setStatus("CHECKED_IN");
        } else {
            refugee.setStatus("CHECKED_OUT");
        }

        boolean saved = refugeeRepo.save(refugee);
        if (saved) {
            // Update capacities
            if (oldShelterId != null) {
                shelterRepo.updateOccupancy(oldShelterId, -1);
            }
            if (targetShelterId != null) {
                shelterRepo.updateOccupancy(targetShelterId, 1);
            }

            // Log movement
            movementRepo.logMovement(refugeeId, oldShelterId, targetShelterId, operatorUsername, notes);
            return true;
        }
        return false;
    }

    public List<RefugeeMovement> getMovementHistory(int refugeeId) {
        return movementRepo.findByRefugeeId(refugeeId);
    }

    public List<RefugeeMovement> getAllMovements() {
        return movementRepo.findAll();
    }
}

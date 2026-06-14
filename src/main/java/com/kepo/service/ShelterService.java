package com.kepo.service;

import com.kepo.model.Shelter;
import com.kepo.repository.ShelterRepository;

import java.util.List;

public class ShelterService {

    private final ShelterRepository shelterRepo;
    private final UserService userService;

    public ShelterService(ShelterRepository shelterRepo, UserService userService) {
        this.shelterRepo = shelterRepo;
        this.userService = userService;
    }

    public List<Shelter> getAllShelters() {
        return shelterRepo.findAll();
    }

    public Shelter getShelterById(int id) {
        return shelterRepo.findById(id);
    }

    public boolean saveShelter(Shelter sh) {
        boolean res = shelterRepo.save(sh);
        if (res && userService.getCurrentUser() != null) {
            String act = sh.getShelterId() > 0 ? "UPDATE_SHELTER" : "CREATE_SHELTER";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Shelter: " + sh.getName() + ", Kapasitas: " + sh.getCapacity());
        }
        return res;
    }

    public boolean deleteShelter(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_SHELTER", "ID: " + id);
        }
        return shelterRepo.delete(id);
    }

    public boolean updateOccupancy(int id, int change) {
        return shelterRepo.updateOccupancy(id, change);
    }
}

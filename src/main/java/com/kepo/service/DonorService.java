package com.kepo.service;

import com.kepo.model.Donor;
import com.kepo.repository.DonorRepository;

import java.util.List;

public class DonorService {

    private final DonorRepository donorRepo;
    private final UserService userService;

    public DonorService(DonorRepository donorRepo, UserService userService) {
        this.donorRepo = donorRepo;
        this.userService = userService;
    }

    public List<Donor> getAllDonors() {
        return donorRepo.findAll();
    }

    public Donor getDonorById(int id) {
        return donorRepo.findById(id);
    }

    public boolean saveDonor(Donor d) {
        boolean res = donorRepo.save(d);
        if (res && userService.getCurrentUser() != null) {
            String act = d.getDonorId() > 0 ? "UPDATE_DONOR" : "CREATE_DONOR";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Donatur: " + d.getDonorName());
        }
        return res;
    }

    public boolean deleteDonor(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_DONOR", "ID: " + id);
        }
        return donorRepo.delete(id);
    }
}

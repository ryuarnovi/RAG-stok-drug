package com.kepo.service;

import com.kepo.model.Refugee;
import com.kepo.repository.RefugeeRepository;

import java.sql.Timestamp;
import java.util.List;

public class RefugeeService {

    private final RefugeeRepository refugeeRepo;
    private final ShelterService shelterService;
    private final UserService userService;

    public RefugeeService(RefugeeRepository refugeeRepo, ShelterService shelterService, UserService userService) {
        this.refugeeRepo = refugeeRepo;
        this.shelterService = shelterService;
        this.userService = userService;
    }

    public List<Refugee> getAllRefugees() {
        return refugeeRepo.findAll();
    }

    public Refugee getRefugeeById(int id) {
        return refugeeRepo.findById(id);
    }

    public Refugee getRefugeeByNik(String nik) {
        return refugeeRepo.findByNik(nik);
    }

    public List<Refugee> getRefugeesByShelter(int shelterId) {
        return refugeeRepo.findByShelter(shelterId);
    }

    public boolean saveRefugee(Refugee r) {
        Refugee old = null;
        if (r.getRefugeeId() > 0) {
            old = refugeeRepo.findById(r.getRefugeeId());
        }

        boolean res = refugeeRepo.save(r);
        if (res) {
            // Adjust occupancy based on check-in/out transitions
            if (old == null) {
                // New registration
                if ("CHECKED_IN".equals(r.getStatus()) && r.getShelterId() != null) {
                    shelterService.updateOccupancy(r.getShelterId(), 1);
                }
            } else {
                // Update existing
                String oldStatus = old.getStatus();
                String newStatus = r.getStatus();
                Integer oldShelter = old.getShelterId();
                Integer newShelter = r.getShelterId();

                if ("CHECKED_IN".equals(oldStatus) && "CHECKED_OUT".equals(newStatus)) {
                    if (oldShelter != null) {
                        shelterService.updateOccupancy(oldShelter, -1);
                    }
                } else if ("CHECKED_OUT".equals(oldStatus) && "CHECKED_IN".equals(newStatus)) {
                    if (newShelter != null) {
                        shelterService.updateOccupancy(newShelter, 1);
                    }
                } else if ("CHECKED_IN".equals(oldStatus) && "CHECKED_IN".equals(newStatus)) {
                    // Shelter changed
                    if (oldShelter != null && !oldShelter.equals(newShelter)) {
                        shelterService.updateOccupancy(oldShelter, -1);
                    }
                    if (newShelter != null && !newShelter.equals(oldShelter)) {
                        shelterService.updateOccupancy(newShelter, 1);
                    }
                }
            }

            if (userService.getCurrentUser() != null) {
                String act = old != null ? "UPDATE_REFUGEE" : "REGISTER_REFUGEE";
                userService.logActivity(userService.getCurrentUser().getUsername(), act, "Pengungsi: " + r.getName() + " (NIK: " + r.getNik() + ")");
            }
        }
        return res;
    }

    public boolean checkIn(Refugee r, int shelterId) {
        r.setStatus("CHECKED_IN");
        r.setShelterId(shelterId);
        r.setCheckInTime(new Timestamp(System.currentTimeMillis()));
        r.setCheckOutTime(null);
        return saveRefugee(r);
    }

    public boolean checkOut(Refugee r) {
        r.setStatus("CHECKED_OUT");
        r.setCheckOutTime(new Timestamp(System.currentTimeMillis()));
        return saveRefugee(r);
    }

    public boolean deleteRefugee(int id) {
        Refugee r = refugeeRepo.findById(id);
        if (r != null) {
            if ("CHECKED_IN".equals(r.getStatus()) && r.getShelterId() != null) {
                shelterService.updateOccupancy(r.getShelterId(), -1);
            }
            if (userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_REFUGEE", "NIK: " + r.getNik());
            }
            return refugeeRepo.delete(id);
        }
        return false;
    }
}

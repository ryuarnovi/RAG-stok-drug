package com.kepo.service;

import com.kepo.model.Distribution;
import com.kepo.repository.DistributionRepository;

import java.util.List;

public class DistributionService {

    private final DistributionRepository distributionRepo;
    private final UserService userService;

    public DistributionService(DistributionRepository distributionRepo, UserService userService) {
        this.distributionRepo = distributionRepo;
        this.userService = userService;
    }

    public List<Distribution> getAllDistributions() {
        return distributionRepo.findAll();
    }

    public Distribution getDistributionById(int id) {
        return distributionRepo.findById(id);
    }

    public Distribution getDistributionByDocNum(String docNum) {
        return distributionRepo.findByDocNum(docNum);
    }

    public boolean saveDistribution(Distribution d) {
        boolean isNew = d.getDistributionId() <= 0;
        boolean res = distributionRepo.save(d);
        if (res && userService.getCurrentUser() != null) {
            String act = isNew ? "CREATE_DISTRIBUTION" : "UPDATE_DISTRIBUTION";
            userService.logActivity(userService.getCurrentUser().getUsername(), act, "Doc: " + d.getDocNum() + ", Item: " + d.getItemType() + " (Qty: " + d.getQuantity() + ")");
        }
        return res;
    }

    public boolean deleteDistribution(int id) {
        if (userService.getCurrentUser() != null) {
            userService.logActivity(userService.getCurrentUser().getUsername(), "DELETE_DISTRIBUTION", "ID: " + id);
        }
        return distributionRepo.delete(id);
    }

    public boolean approveDistribution(int id) {
        Distribution d = distributionRepo.findById(id);
        if (d != null && "DRAFT".equals(d.getStatus())) {
            d.setStatus("APPROVED");
            boolean res = distributionRepo.save(d);
            if (res && userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "APPROVE_DISTRIBUTION", "Doc: " + d.getDocNum());
            }
            return res;
        }
        return false;
    }

    public boolean shipDistribution(int id) {
        Distribution d = distributionRepo.findById(id);
        if (d != null && "APPROVED".equals(d.getStatus())) {
            d.setStatus("SHIPPED");
            boolean res = distributionRepo.save(d);
            if (res && userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "SHIP_DISTRIBUTION", "Doc: " + d.getDocNum());
            }
            return res;
        }
        return false;
    }

    public boolean receiveDistribution(int id) {
        Distribution d = distributionRepo.findById(id);
        if (d != null && "SHIPPED".equals(d.getStatus())) {
            d.setStatus("RECEIVED");
            boolean res = distributionRepo.save(d);
            if (res && userService.getCurrentUser() != null) {
                userService.logActivity(userService.getCurrentUser().getUsername(), "RECEIVE_DISTRIBUTION", "Doc: " + d.getDocNum());
            }
            return res;
        }
        return false;
    }
}

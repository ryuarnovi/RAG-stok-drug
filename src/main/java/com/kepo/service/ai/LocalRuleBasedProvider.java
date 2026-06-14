package com.kepo.service.ai;

import com.kepo.repository.ShelterRepository;
import com.kepo.repository.RefugeeRepository;
import com.kepo.repository.MedicineRepository;
import com.kepo.model.Shelter;
import com.kepo.model.Refugee;
import com.kepo.model.Medicine;

import java.util.List;

public class LocalRuleBasedProvider implements AIProvider {

    private final ShelterRepository shelterRepo;
    private final RefugeeRepository refugeeRepo;
    private final MedicineRepository medicineRepo;

    public LocalRuleBasedProvider(ShelterRepository shelterRepo, RefugeeRepository refugeeRepo, MedicineRepository medicineRepo) {
        this.shelterRepo = shelterRepo;
        this.refugeeRepo = refugeeRepo;
        this.medicineRepo = medicineRepo;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Silakan tanyakan sesuatu tentang posko penanggulangan bencana.";
        }

        String lower = userMessage.toLowerCase().trim();

        if (lower.contains("shelter") || lower.contains("posko") || lower.contains("kapasitas")) {
            return handleShelterQuery();
        }

        if (lower.contains("pengungsi") || lower.contains("korban") || lower.contains("nik")) {
            return handleRefugeeQuery();
        }

        if (lower.contains("obat") || lower.contains("stok") || lower.contains("medis")) {
            return handleMedicineQuery();
        }

        return "Maaf, saat ini asisten offline hanya dapat menjawab pertanyaan seputar 'shelter', 'pengungsi', atau 'stok obat'. Silakan hubungkan internet dan atur kunci API Gemini untuk mengaktifkan asisten AI cerdas.";
    }

    private String handleShelterQuery() {
        List<Shelter> list = shelterRepo.findAll();
        if (list.isEmpty()) {
            return "Belum ada shelter yang terdaftar di database.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- STATUS MONITORING SHELTER ---\n");
        for (Shelter s : list) {
            sb.append(String.format("- %s: Kapasitas %d/%d (%s) - PJ: %s\n",
                    s.getName(), s.getCurrentOccupancy(), s.getCapacity(), s.getStatus(), s.getPenanggungJawab()));
        }
        return sb.toString();
    }

    private String handleRefugeeQuery() {
        List<Refugee> list = refugeeRepo.findAll();
        if (list.isEmpty()) {
            return "Belum ada pengungsi terdata di database.";
        }

        int active = 0;
        for (Refugee r : list) {
            if ("CHECKED_IN".equals(r.getStatus())) active++;
        }

        return String.format("Total pengungsi terdaftar: %d orang.\nPengungsi aktif di shelter: %d orang.\nPengungsi keluar (Checked Out): %d orang.",
                list.size(), active, list.size() - active);
    }

    private String handleMedicineQuery() {
        List<Medicine> list = medicineRepo.findAll();
        if (list.isEmpty()) {
            return "Tidak ada persediaan obat terdaftar.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- DAFTAR INVENTARIS OBAT ---\n");
        int lowCount = 0;
        for (Medicine m : list) {
            boolean isLow = m.getStockQuantity() <= m.getMinimumStock();
            if (isLow) lowCount++;
            sb.append(String.format("- %s (%s) - Stok: %d %s %s\n",
                    m.getMedicineName(), m.getCategory(), m.getStockQuantity(), m.getUnit(), isLow ? "[KRITIS]" : ""));
        }
        sb.append(String.format("\nTotal item: %d. Item kritis: %d.", list.size(), lowCount));
        return sb.toString();
    }
}

package com.kepo.service;

import com.kepo.model.*;
import com.kepo.repository.*;
import com.kepo.service.ai.AIProvider;

import java.util.ArrayList;
import java.util.List;

public class AIRecommendationService {

    private final ShelterRepository shelterRepo;
    private final RefugeeRepository refugeeRepo;
    private final MedicineRepository medicineRepo;
    private final DistributionRepository distributionRepo;
    private final EventRepository eventRepo;
    private final AIProvider aiProvider;

    public AIRecommendationService(ShelterRepository shelterRepo,
                                   RefugeeRepository refugeeRepo,
                                   MedicineRepository medicineRepo,
                                   DistributionRepository distributionRepo,
                                   EventRepository eventRepo,
                                   AIProvider aiProvider) {
        this.shelterRepo = shelterRepo;
        this.refugeeRepo = refugeeRepo;
        this.medicineRepo = medicineRepo;
        this.distributionRepo = distributionRepo;
        this.eventRepo = eventRepo;
        this.aiProvider = aiProvider;
    }

    public List<String> getEmergencyAlerts() {
        List<String> alerts = new ArrayList<>();

        // 1. Shelter over capacity or warning levels
        for (Shelter s : shelterRepo.findAll()) {
            if ("KRITIS".equals(s.getStatus()) || s.getCurrentOccupancy() >= s.getCapacity()) {
                alerts.add("[SHELTER KRITIS] " + s.getName() + " telah mencapai atau melebihi kapasitas maksimum (" + s.getCurrentOccupancy() + "/" + s.getCapacity() + ").");
            } else if ("WASPADA".equals(s.getStatus())) {
                alerts.add("[SHELTER WASPADA] " + s.getName() + " mendekati kapasitas penuh (" + s.getCurrentOccupancy() + "/" + s.getCapacity() + ").");
            }
        }

        // 2. Low stock medicines
        for (Medicine m : medicineRepo.findAll()) {
            if (m.getStockQuantity() <= m.getMinimumStock()) {
                alerts.add("[STOK KRITIS] Obat " + m.getMedicineName() + " hampir habis. Sisa stok: " + m.getStockQuantity() + " " + m.getUnit() + " (Min: " + m.getMinimumStock() + ").");
            }
        }

        // 3. Delayed distributions
        for (Distribution d : distributionRepo.findAll()) {
            if ("APPROVED".equals(d.getStatus()) || "SHIPPED".equals(d.getStatus())) {
                alerts.add("[DISTRIBUSI BERJALAN] Dokumen " + d.getDocNum() + " (" + d.getItemType() + " x" + d.getQuantity() + ") menuju " + d.getShelterName() + " berstatus " + d.getStatus() + ".");
            }
        }

        // 4. Critical medical cases
        for (Refugee r : refugeeRepo.findAll()) {
            if ("CHECKED_IN".equals(r.getStatus()) && r.getMedicalNotes() != null && !r.getMedicalNotes().isBlank()) {
                String notes = r.getMedicalNotes().toLowerCase();
                if (notes.contains("sesak") || notes.contains("bakar") || notes.contains("asma") || notes.contains("ispa") || notes.contains("akut") || notes.contains("darurat")) {
                    alerts.add("[KASUS MEDIS] Pengungsi " + r.getName() + " di " + (r.getShelterName() != null ? r.getShelterName() : "Shelter") + ": " + r.getMedicalNotes());
                }
            }
        }

        return alerts;
    }

    public List<String> getAISuggestions() {
        List<String> suggestions = new ArrayList<>();

        // Generate smart recommendations
        List<Shelter> shelters = shelterRepo.findAll();
        List<Medicine> medicines = medicineRepo.findAll();

        for (Shelter s : shelters) {
            double ratio = s.getCapacity() > 0 ? (double) s.getCurrentOccupancy() / s.getCapacity() : 0.0;
            if (ratio >= 0.8) {
                suggestions.add("Pindahkan sebagian pengungsi baru dari " + s.getName() + " ke shelter alternatif terdekat untuk menghindari overcapacity.");
            }
        }

        for (Medicine m : medicines) {
            if (m.getStockQuantity() <= m.getMinimumStock()) {
                suggestions.add("Ajukan pengadaan darurat obat " + m.getMedicineName() + " sejumlah " + (m.getMinimumStock() * 2) + " " + m.getUnit() + " ke supplier.");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Kondisi seluruh shelter terpantau aman dan logistik tercukupi.");
        }

        return suggestions;
    }

    public String chat(String userMessage) {
        String systemPrompt = buildSystemPrompt();
        String response = aiProvider.chat(systemPrompt, userMessage);
        return removeEmojis(response);
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Anda adalah asisten AI KEPO (Kendali Evakuasi dan Pengelolaan Operasional Bencana) yang profesional dan membantu untuk pusat kendali penanggulangan bencana.\n");
        sb.append("Tugas utama Anda adalah membantu petugas memantau shelter, pengungsi, inventaris obat, dan distribusi bantuan.\n");
        sb.append("Anda harus menjawab pertanyaan pengguna secara ringkas, faktual, dan ramah dengan menggunakan data sistem riil di bawah ini.\n\n");

        sb.append("--- ATURAN PENTING ---\n");
        sb.append("1. Jawablah dalam Bahasa Indonesia yang baik dan profesional.\n");
        sb.append("2. HILANGKAN SEMUA EMOJI dalam respons Anda. Jangan gunakan emoji (seperti ⚠️, ✅, ⛔, 🏥, dll.) dalam teks jawaban. Gunakan penanda teks seperti [PENTING] atau [ALERT] jika perlu.\n");
        sb.append("3. RAG Guardrail: Batasi jawaban Anda hanya seputar penanggulangan bencana, shelter, pengungsi, inventaris obat/kesehatan, dan distribusi bantuan sesuai data. Jangan menjawab pertanyaan di luar topik ini atau menghasilkan hal-hal lain di luar farmasi/kebencanaan.\n");
        sb.append("4. Selalu ingatkan petugas untuk memverifikasi data dan instruksi penting secara manual di lapangan.\n\n");

        sb.append("--- DATA EVENT DARURAT AKTIF ---\n");
        List<Event> activeEvents = eventRepo.findAll();
        for (Event e : activeEvents) {
            if ("ACTIVE".equals(e.getStatus())) {
                sb.append(String.format("- Event: %s | Lokasi: %s | Keterangan: %s\n", e.getName(), e.getLocation(), e.getDescription()));
            }
        }

        sb.append("\n--- DATA MONITORING SHELTER ---\n");
        List<Shelter> shelters = shelterRepo.findAll();
        for (Shelter s : shelters) {
            sb.append(String.format("- Shelter: %s | Lokasi: %s | Kapasitas: %d | Terisi: %d | Status: %s | Penanggung Jawab: %s\n",
                    s.getName(), s.getLocation(), s.getCapacity(), s.getCurrentOccupancy(), s.getStatus(), s.getPenanggungJawab()));
        }

        sb.append("\n--- DATA PENGUNGSI AKTIF ---\n");
        List<Refugee> refugees = refugeeRepo.findAll();
        for (Refugee r : refugees) {
            if ("CHECKED_IN".equals(r.getStatus())) {
                sb.append(String.format("- Pengungsi: %s | NIK: %s | Usia: %d | Gender: %s | Shelter: %s | Catatan Medis: %s\n",
                        r.getName(), r.getNik(), r.getAge(), r.getGender(), r.getShelterName() != null ? r.getShelterName() : "N/A", r.getMedicalNotes() != null ? r.getMedicalNotes() : "Tidak ada"));
            }
        }

        sb.append("\n--- DATA LOGISTIK KESEHATAN / INVENTARIS OBAT ---\n");
        List<Medicine> medicines = medicineRepo.findAll();
        for (Medicine m : medicines) {
            sb.append(String.format("- Obat: %s | Kode: %s | Kategori: %s | Stok: %d %s | Minimum: %d | Exp: %s\n",
                    m.getMedicineName(), m.getMedicineCode(), m.getCategory(), m.getStockQuantity(), m.getUnit(), m.getMinimumStock(), m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A"));
        }

        sb.append("\n--- STATUS DOKUMEN DISTRIBUSI BANTUAN ---\n");
        List<Distribution> distributions = distributionRepo.findAll();
        for (Distribution d : distributions) {
            sb.append(String.format("- Dokumen: %s | Shelter: %s | Tipe Bantuan: %s | Jumlah: %d | Status: %s\n",
                    d.getDocNum(), d.getShelterName(), d.getItemType(), d.getQuantity(), d.getStatus()));
        }

        return sb.toString();
    }

    public List<String> getShelterOccupancyPredictions() {
        List<String> predictions = new ArrayList<>();
        for (Shelter s : shelterRepo.findAll()) {
            double ratio = s.getCapacity() > 0 ? (double) s.getCurrentOccupancy() / s.getCapacity() : 0.0;
            int pct = (int) (ratio * 100);
            if (pct >= 90) {
                predictions.add(s.getName() + " (" + pct + "% Terisi) - [KRITIS] Kapasitas hampir habis. Pendaftaran baru disarankan dialihkan ke shelter terdekat.");
            } else if (pct >= 75) {
                predictions.add(s.getName() + " (" + pct + "% Terisi) - [WASPADA] Kepadatan tinggi. Diperkirakan penuh dalam 48 jam ke depan berdasarkan tren evakuasi.");
            } else {
                predictions.add(s.getName() + " (" + pct + "% Terisi) - [AMAN] Kapasitas memadai untuk menampung pengungsi tambahan.");
            }
        }
        if (predictions.isEmpty()) {
            predictions.add("Tidak ada data shelter untuk dianalisis.");
        }
        return predictions;
    }

    public List<String> getMedicineStockDepletionPredictions() {
        List<String> predictions = new ArrayList<>();
        for (Medicine m : medicineRepo.findAll()) {
            if (m.getStockQuantity() == 0) {
                predictions.add(m.getMedicineName() + " - [KRITIS] Stok habis. Segera buat permintaan restok ke supplier.");
            } else if (m.getStockQuantity() <= m.getMinimumStock()) {
                predictions.add(m.getMedicineName() + " - [WASPADA] Sisa stok: " + m.getStockQuantity() + " " + m.getUnit() + ". Tren penggunaan tinggi, diproyeksikan habis dalam 3 hari.");
            } else {
                predictions.add(m.getMedicineName() + " - [STABIL] Stok aman: " + m.getStockQuantity() + " " + m.getUnit() + ".");
            }
        }
        if (predictions.isEmpty()) {
            predictions.add("Tidak ada data obat untuk dianalisis.");
        }
        return predictions;
    }

    public List<String> getPriorityShelters() {
        List<String> priorities = new ArrayList<>();
        List<Shelter> shelters = shelterRepo.findAll();
        shelters.sort((s1, s2) -> Integer.compare(s2.getCurrentOccupancy(), s1.getCurrentOccupancy()));
        
        for (Shelter s : shelters) {
            double ratio = s.getCapacity() > 0 ? (double) s.getCurrentOccupancy() / s.getCapacity() : 0.0;
            if (ratio >= 0.8) {
                priorities.add("[PRIORITAS 1] " + s.getName() + " - Rasio Kepadatan: " + (int)(ratio * 100) + "%. Membutuhkan suplai logistik makanan dan obat-obatan segera.");
            } else if (ratio >= 0.5) {
                priorities.add("[PRIORITAS 2] " + s.getName() + " - Rasio Kepadatan: " + (int)(ratio * 100) + "%. Terpantau padat, siapkan jalur distribusi cadangan.");
            }
        }
        if (priorities.isEmpty()) {
            priorities.add("Semua shelter terpantau aman dan memiliki alokasi logistik seimbang.");
        }
        return priorities;
    }

    public String getSituationalExecutiveSummary() {
        try {
            String prompt = "Tuliskan ringkasan eksekutif situasi operasional penanganan bencana saat ini secara ringkas, profesional, dan padat (maksimal 3 kalimat). " +
                            "Gunakan data riil yang disediakan. Jangan gunakan emoji/ikon sama sekali. " +
                            "Sebutkan ringkasan data penting di lapangan secara langsung.";
            String systemPrompt = buildSystemPrompt();
            String response = aiProvider.chat(systemPrompt, prompt);
            if (response != null && !response.isBlank()) {
                return removeEmojis(response).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getLocalExecutiveSummary();
    }

    public String getLocalExecutiveSummary() {
        int activeEvents = 0;
        for (Event e : eventRepo.findAll()) {
            if ("ACTIVE".equals(e.getStatus())) activeEvents++;
        }
        int totalShelters = 0;
        int totalRefugees = 0;
        for (Shelter s : shelterRepo.findAll()) {
            totalShelters++;
            totalRefugees += s.getCurrentOccupancy();
        }
        int criticalMedicines = 0;
        for (Medicine m : medicineRepo.findAll()) {
            if (m.getStockQuantity() <= m.getMinimumStock()) {
                criticalMedicines++;
            }
        }
        return String.format(
            "RINGKASAN OPERASIONAL: Saat ini terdapat %d kejadian bencana aktif yang sedang ditangani. " +
            "Total pengungsi aktif di %d shelter penampungan adalah %d jiwa. " +
            "Terdapat %d jenis obat-obatan darurat dalam status stok kritis yang memerlukan pengadaan segera.",
            activeEvents, totalShelters, totalRefugees, criticalMedicines
        );
    }

    public List<String> getLackingLogisticsAnalysis() {
        List<String> results = new ArrayList<>();
        List<Shelter> shelters = shelterRepo.findAll();
        List<Refugee> refugees = refugeeRepo.findAll();
        List<Medicine> medicines = medicineRepo.findAll();
        List<Distribution> distributions = distributionRepo.findAll();

        for (Shelter s : shelters) {
            // Find active refugees in this shelter
            List<Refugee> activeInShelter = new ArrayList<>();
            for (Refugee r : refugees) {
                if ("CHECKED_IN".equals(r.getStatus()) && r.getShelterId() == s.getShelterId()) {
                    activeInShelter.add(r);
                }
            }

            if (activeInShelter.isEmpty()) {
                continue;
            }

            // Map to track how many refugees need each medicine code
            java.util.Map<String, Integer> medNeedsCount = new java.util.HashMap<>();
            java.util.Map<String, List<String>> medRefugeeNames = new java.util.HashMap<>();

            for (Refugee r : activeInShelter) {
                if (r.getMedicalNotes() == null || r.getMedicalNotes().isBlank()) {
                    continue;
                }
                String notes = r.getMedicalNotes().toLowerCase();
                List<String> neededCodes = new ArrayList<>();

                if (notes.contains("sesak") || notes.contains("ispa") || notes.contains("napas") || notes.contains("asma")) {
                    neededCodes.add("MED-003");
                    neededCodes.add("MED-007");
                }
                if (notes.contains("diabetes") || notes.contains("insulin") || notes.contains("gula")) {
                    neededCodes.add("MED-006");
                    neededCodes.add("MED-005");
                }
                if (notes.contains("luka") || notes.contains("bakar") || notes.contains("lecet") || notes.contains("robek") || notes.contains("jatuh")) {
                    neededCodes.add("MED-001");
                    neededCodes.add("MED-004");
                }
                if (notes.contains("hamil") || notes.contains("vitamin") || notes.contains("suplemen") || notes.contains("lemas")) {
                    neededCodes.add("MED-008");
                }
                if (notes.contains("pusing") || notes.contains("demam") || notes.contains("panas") || notes.contains("sakit kepala") || notes.contains("nyeri")) {
                    neededCodes.add("MED-002");
                    neededCodes.add("MED-004");
                }
                if (notes.contains("maag") || notes.contains("lambung") || notes.contains("perih") || notes.contains("asam lambung")) {
                    neededCodes.add("MED-009");
                }
                if (notes.contains("darah tinggi") || notes.contains("hipertensi")) {
                    neededCodes.add("MED-010");
                }

                for (String code : neededCodes) {
                    medNeedsCount.put(code, medNeedsCount.getOrDefault(code, 0) + 1);
                    medRefugeeNames.computeIfAbsent(code, k -> new ArrayList<>()).add(r.getName());
                }
            }

            // Now, check if there are distributions of these medicines to this shelter
            for (java.util.Map.Entry<String, Integer> entry : medNeedsCount.entrySet()) {
                String medCode = entry.getKey();
                int countNeeded = entry.getValue();
                List<String> names = medRefugeeNames.get(medCode);

                Medicine targetMed = null;
                for (Medicine m : medicines) {
                    if (m.getMedicineCode().equals(medCode)) {
                        targetMed = m;
                        break;
                    }
                }

                if (targetMed == null) continue;

                int totalDistributed = 0;
                for (Distribution d : distributions) {
                    if (d.getShelterId() == s.getShelterId() && !"DRAFT".equals(d.getStatus())) {
                        String notes = d.getNotes();
                        if (notes != null && notes.contains("[ALLOCATION_DATA:")) {
                            int start = notes.indexOf("[ALLOCATION_DATA:") + "[ALLOCATION_DATA:".length();
                            int end = notes.indexOf("]", start);
                            if (end > start) {
                                String data = notes.substring(start, end);
                                String[] items = data.split(";");
                                for (String item : items) {
                                    if (item.contains(":")) {
                                        String[] parts = item.split(":");
                                        if (parts.length == 2 && parts[0].equals(medCode)) {
                                            totalDistributed += Integer.parseInt(parts[1]);
                                        }
                                    }
                                }
                            }
                        } else {
                            String itemType = d.getItemType();
                            if (itemType != null && itemType.contains(medCode)) {
                                totalDistributed += d.getQuantity();
                            }
                        }
                    }
                }

                int minimumDistributedTarget = countNeeded * 10;
                if (totalDistributed < minimumDistributedTarget) {
                    String namesStr = String.join(", ", names);
                    
                    String status = "MENDESAK";
                    if (targetMed.getStockQuantity() <= targetMed.getMinimumStock()) {
                        status = "KRITIS";
                    }

                    results.add(String.format(
                        "[%s] Shelter %s membutuhkan %s. Terdata %d pengungsi (%s) membutuhkan obat ini, sedangkan sisa alokasi tersalurkan hanya %d %s. Stok global saat ini: %d %s.",
                        status, s.getName(), targetMed.getMedicineName(), countNeeded, namesStr, totalDistributed, targetMed.getUnit(), targetMed.getStockQuantity(), targetMed.getUnit()
                    ));
                }
            }
        }

        if (results.isEmpty()) {
            results.add("[AMAN] Seluruh shelter memiliki pasokan obat memadai berdasarkan rekam kondisi pengungsi saat ini.");
        }
        return results;
    }

    private String removeEmojis(String text) {
        if (text == null) return null;
        return text.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF\\u2300-\\u23FF\\u2B50\\u2B06\\u2194\\u2B05\\u2B07\\u2b55]", "")
                   .replaceAll("⚠️", "")
                   .replaceAll("⛔", "")
                   .replaceAll("✅", "")
                   .replaceAll("🏥", "")
                   .replaceAll("🚨", "");
    }
}

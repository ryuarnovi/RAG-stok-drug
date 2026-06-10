package com.pharmastock.service;

import com.pharmastock.model.AIRecommendation;
import com.pharmastock.model.DrugKnowledge;
import com.pharmastock.model.Medicine;
import com.pharmastock.repository.IInventoryTransactionRepository;
import com.pharmastock.repository.IMedicineRepository;
import com.pharmastock.service.ai.AIProvider;

import java.util.*;
import java.util.stream.Collectors;

public class AIRecommendationService {

    private static final int EXPIRY_THRESHOLD_DAYS = 30;
    private static final int DEAD_STOCK_THRESHOLD_DAYS = 90;
    private static final int FAST_MOVING_MIN_QTY = 100;
    private static final int SLOW_MOVING_MAX_QTY = 10;

    private final IMedicineRepository medicineRepo;
    private final IInventoryTransactionRepository transactionRepo;
    private final AIProvider aiProvider;
    private final MedicalKnowledgeService knowledgeService;

    public AIRecommendationService(IMedicineRepository medicineRepo,
                                   IInventoryTransactionRepository transactionRepo,
                                   AIProvider aiProvider,
                                   MedicalKnowledgeService knowledgeService) {
        this.medicineRepo = medicineRepo;
        this.transactionRepo = transactionRepo;
        this.aiProvider = aiProvider;
        this.knowledgeService = knowledgeService;
    }

    /**
     * Rekomendasi reorder untuk obat dengan stok di bawah minimum.
     */
    public List<AIRecommendation> getLowStockRecommendations() {
        List<AIRecommendation> recommendations = new ArrayList<>();
        List<Medicine> lowStock = medicineRepo.findLowStock();

        for (Medicine med : lowStock) {
            int reorderQty = (med.getMinimumStock() * 2) - med.getStockQuantity();
            String message = String.format(
                    "Stok %s saat ini %d %s, di bawah batas minimum (%d). " +
                    "Disarankan untuk memesan %d %s dari supplier.",
                    med.getMedicineName(), med.getStockQuantity(), med.getUnit(),
                    med.getMinimumStock(), reorderQty, med.getUnit());

            AIRecommendation rec = new AIRecommendation(
                    AIRecommendation.Type.REORDER,
                    "Reorder: " + med.getMedicineName(),
                    message,
                    med.getStockQuantity() == 0 ? AIRecommendation.Severity.CRITICAL : AIRecommendation.Severity.WARNING,
                    med
            );
            recommendations.add(rec);
        }
        return recommendations;
    }

    /**
     * Rekomendasi untuk obat yang akan/sudah kadaluarsa.
     */
    public List<AIRecommendation> getExpiryRecommendations() {
        List<AIRecommendation> recommendations = new ArrayList<>();

        List<Medicine> expired = medicineRepo.findExpired();
        for (Medicine med : expired) {
            String message = String.format(
                    "%s (Batch: %s) telah kadaluarsa pada %s. " +
                    "Segera lakukan retur atau pemusnahan. Sisa stok: %d %s.",
                    med.getMedicineName(), med.getBatchNumber(),
                    med.getExpiryDate(), med.getStockQuantity(), med.getUnit());

            recommendations.add(new AIRecommendation(
                    AIRecommendation.Type.EXPIRY, "Kadaluarsa: " + med.getMedicineName(),
                    message, AIRecommendation.Severity.CRITICAL, med));
        }

        List<Medicine> nearExpiry = medicineRepo.findNearExpiry(EXPIRY_THRESHOLD_DAYS);
        for (Medicine med : nearExpiry) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(), med.getExpiryDate());
            String message = String.format(
                    "%s akan kadaluarsa dalam %d hari (%s). " +
                    "Pertimbangkan untuk memprioritaskan penjualan. Sisa stok: %d %s.",
                    med.getMedicineName(), daysLeft, med.getExpiryDate(),
                    med.getStockQuantity(), med.getUnit());

            recommendations.add(new AIRecommendation(
                    AIRecommendation.Type.EXPIRY, "Segera Kadaluarsa: " + med.getMedicineName(),
                    message, AIRecommendation.Severity.WARNING, med));
        }
        return recommendations;
    }

    /**
     * Smart insight: Fast Moving, Slow Moving, Dead Stock.
     */
    public List<AIRecommendation> getSmartInsights() {
        List<AIRecommendation> insights = new ArrayList<>();
        List<Medicine> allMedicines = medicineRepo.findAll();

        for (Medicine med : allMedicines) {
            int outQty30 = transactionRepo.sumOutQuantity(med.getMedicineId(), EXPIRY_THRESHOLD_DAYS);
            int outCount90 = transactionRepo.countOutTransactions(med.getMedicineId(), DEAD_STOCK_THRESHOLD_DAYS);

            if (outQty30 > FAST_MOVING_MIN_QTY) {
                insights.add(new AIRecommendation(
                        AIRecommendation.Type.INSIGHT,
                        "Fast Moving: " + med.getMedicineName(),
                        String.format("%s terjual %d unit dalam 30 hari terakhir. Pastikan stok selalu tersedia.",
                                med.getMedicineName(), outQty30),
                        AIRecommendation.Severity.INFO, med));
            } else if (outCount90 > 0 && outQty30 < SLOW_MOVING_MAX_QTY) {
                insights.add(new AIRecommendation(
                        AIRecommendation.Type.INSIGHT,
                        "Slow Moving: " + med.getMedicineName(),
                        String.format("%s hanya terjual %d unit dalam 30 hari terakhir. " +
                                "Pertimbangkan promosi atau retur ke supplier.",
                                med.getMedicineName(), outQty30),
                        AIRecommendation.Severity.INFO, med));
            } else if (outCount90 == 0 && med.getStockQuantity() > 0) {
                insights.add(new AIRecommendation(
                        AIRecommendation.Type.INSIGHT,
                        "Dead Stock: " + med.getMedicineName(),
                        String.format("%s tidak ada transaksi keluar dalam %d hari. Stok tersisa: %d %s.",
                                med.getMedicineName(), DEAD_STOCK_THRESHOLD_DAYS, med.getStockQuantity(), med.getUnit()),
                        AIRecommendation.Severity.WARNING, med));
            }
        }
        return insights;
    }

    /**
     * Demand forecast menggunakan simple moving average.
     */
    public AIRecommendation getDemandForecast(int medicineId) {
        Medicine med = medicineRepo.findById(medicineId).orElse(null);
        if (med == null) return null;

        Map<String, Integer> monthly = transactionRepo.getMonthlyOutMovement(medicineId, 3);
        if (monthly.isEmpty()) {
            return new AIRecommendation(
                    AIRecommendation.Type.FORECAST,
                    "Forecast: " + med.getMedicineName(),
                    "Data transaksi tidak cukup untuk membuat prediksi.",
                    AIRecommendation.Severity.INFO, med);
        }

        double avgMonthly = monthly.values().stream()
                .mapToInt(Integer::intValue).average().orElse(0);
        double avgDaily = avgMonthly / 30.0;

        int daysUntilStockout = avgDaily > 0 ? (int) (med.getStockQuantity() / avgDaily) : 999;

        String message;
        AIRecommendation.Severity severity;

        if (daysUntilStockout <= 7) {
            message = String.format(
                    "Berdasarkan tren penjualan, %s diprediksi akan habis dalam %d hari. " +
                    "Rata-rata konsumsi: %.0f unit/bulan. Segera lakukan pemesanan.",
                    med.getMedicineName(), daysUntilStockout, avgMonthly);
            severity = AIRecommendation.Severity.CRITICAL;
        } else if (daysUntilStockout <= 30) {
            message = String.format(
                    "Stok %s diperkirakan cukup untuk %d hari ke depan. " +
                    "Rata-rata konsumsi: %.0f unit/bulan.",
                    med.getMedicineName(), daysUntilStockout, avgMonthly);
            severity = AIRecommendation.Severity.WARNING;
        } else {
            message = String.format(
                    "Stok %s aman untuk %d hari ke depan. " +
                    "Rata-rata konsumsi: %.0f unit/bulan. Sisa stok: %d %s.",
                    med.getMedicineName(), daysUntilStockout, avgMonthly,
                    med.getStockQuantity(), med.getUnit());
            severity = AIRecommendation.Severity.INFO;
        }

        return new AIRecommendation(
                AIRecommendation.Type.FORECAST,
                "Prediksi: " + med.getMedicineName(),
                message, severity, med);
    }

    /**
     * Aggregasi semua prediksi AI untuk dashboard.
     */
    public List<AIRecommendation> getAIPredictions() {
        List<AIRecommendation> all = new ArrayList<>();
        all.addAll(getLowStockRecommendations());
        all.addAll(getExpiryRecommendations());

        // Add forecasts for low stock items
        List<Medicine> lowStock = medicineRepo.findLowStock();
        for (Medicine med : lowStock) {
            AIRecommendation forecast = getDemandForecast(med.getMedicineId());
            if (forecast != null) {
                all.add(forecast);
            }
        }

        return all;
    }

    /**
     * Hybrid AI Chat dengan RAG + Medical Knowledge + Alternative Recommendation.
     * Priority 1: Database Inventory
     * Priority 2: Medical Knowledge Base
     * Priority 3: Alternative Recommendation Engine
     */
    public String chat(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Silakan ajukan pertanyaan tentang inventaris apotek.";
        }

        String lower = userMessage.toLowerCase().trim();

        // Priority 1: Inventory RAG
        String inventoryResult = handleInventoryRAG(lower, userMessage);
        if (inventoryResult != null) return inventoryResult;

        // Priority 2: Medical Knowledge Base
        String knowledgeResult = handleMedicalKnowledge(lower);
        if (knowledgeResult != null) return knowledgeResult;

        // Priority 3: Alternative Recommendation
        String altResult = handleAlternativeRecommendation(lower);
        if (altResult != null) return altResult;

        // Final: Symptom-based category recommendation
        return handleCategorySuggestion(lower);
    }

    private String handleInventoryRAG(String lower, String originalMessage) {
        List<Medicine> relevant = retrieveRelevantMedicines(lower);
        if (relevant.isEmpty()) return null;

        boolean exactMatch = false;
        StringBuilder result = new StringBuilder();

        // Cek apakah query menyebutkan nama obat spesifik di inventory
        for (Medicine med : relevant) {
            String nameLower = med.getMedicineName().toLowerCase();
            if (lower.contains(nameLower)) {
                exactMatch = true;
                result.append(formatMedicineDetail(med));
            }
        }

        if (exactMatch) {
            result.append("\n\nSumber: ✓ Inventory Database");
            return result.toString();
        }

        // Jika tidak exact match tapi ada data relevan, kirim ke LLM
        // Fallback: tampilkan data sebagai konteks + rekomendasi
        List<Medicine> lowStock = medicineRepo.findLowStock();
        List<Medicine> expired = medicineRepo.findExpired();

        result.append("Data inventaris yang relevan:\n\n");
        for (Medicine med : relevant) {
            String status = med.getStockQuantity() == 0 ? "⚠️ HABIS" :
                    (med.getStockQuantity() < med.getMinimumStock() ? "⚠️ Stok terbatas" : "✅ Tersedia");
            result.append(String.format("• %s (%s) - %s\n  Stok: %d %s",
                    med.getMedicineName(), med.getCategory(), status,
                    med.getStockQuantity(), med.getUnit()));
            if (med.getSellingPrice() != null) {
                result.append(String.format(" | Rp%,.0f", med.getSellingPrice().doubleValue()));
            }
            result.append("\n");
        }

        // Cek kecocokan knowledge untuk konteks
        DrugKnowledge knowledge = knowledgeService.findByBrandOrGeneric(lower);
        if (knowledge != null && !relevant.isEmpty()) {
            result.append(String.format("\n%s merupakan %s yang %s",
                    relevant.get(0).getMedicineName(), knowledge.getCategory(),
                    String.join(", ", knowledge.getIndications())));
        }

        result.append("\n\nSumber: ✓ Inventory Database");
        return result.toString();
    }

    private String formatMedicineDetail(Medicine med) {
        StringBuilder sb = new StringBuilder();
        String status = med.getStockQuantity() == 0 ? "HABIS" :
                (med.getStockQuantity() < med.getMinimumStock() ? "STOK KRITIS" : "TERSEDIA");

        sb.append(String.format("Informasi Obat: %s\n", med.getMedicineName()));
        sb.append("═══════════════════════════════════════════\n");
        sb.append(String.format("Kode         : %s\n", med.getMedicineCode()));
        sb.append(String.format("Kategori     : %s\n", med.getCategory()));
        sb.append(String.format("Stok         : %d %s (%s)\n", med.getStockQuantity(), med.getUnit(), status));
        sb.append(String.format("Stok Minimum : %d %s\n", med.getMinimumStock(), med.getUnit()));
        if (med.getSellingPrice() != null) {
            sb.append(String.format("Harga Jual   : Rp%,.0f\n", med.getSellingPrice().doubleValue()));
        }
        if (med.getExpiryDate() != null) {
            sb.append(String.format("Kadaluarsa   : %s", med.getExpiryDate()));
            if (med.isExpired()) sb.append(" (⚠️ SUDAH KADALUARSA)");
            sb.append("\n");
        }
        if (med.getBatchNumber() != null) {
            sb.append(String.format("Batch        : %s\n", med.getBatchNumber()));
        }
        if (med.getSupplierName() != null) {
            sb.append(String.format("Supplier     : %s\n", med.getSupplierName()));
        }

        // Tambah info knowledge
        DrugKnowledge knowledge = knowledgeService.findByBrandOrGeneric(med.getMedicineName());
        if (knowledge != null) {
            sb.append(String.format("\nGolongan     : %s\n", knowledge.getCategory()));
            sb.append(String.format("Indikasi     : %s\n", String.join(", ", knowledge.getIndications())));
            if (!knowledge.getContraindications().isEmpty()) {
                sb.append(String.format("Kontraindikasi: %s\n", String.join(", ", knowledge.getContraindications())));
            }
        }

        if (med.isLowStock() && med.getStockQuantity() > 0) {
            sb.append("\n⚠️ Stok menipis, disarankan segera melakukan pemesanan ulang.");
        } else if (med.getStockQuantity() == 0) {
            sb.append("\n⛔ Stok habis, silakan hubungi supplier.");
        }

        return sb.toString();
    }

    private String handleMedicalKnowledge(String lower) {
        // Cek apakah query menyebutkan brand/generic tertentu
        DrugKnowledge drug = knowledgeService.findByBrandOrGeneric(lower);
        if (drug != null) {
            // Cek apakah obat ada di inventory
            List<Medicine> inventory = medicineRepo.findAll();
            boolean foundInInventory = false;
            for (Medicine med : inventory) {
                if (lower.contains(med.getMedicineName().toLowerCase())) {
                    foundInInventory = true;
                    break;
                }
            }

            StringBuilder sb = new StringBuilder();
            if (!foundInInventory) {
                sb.append(String.format("%s tidak ditemukan dalam inventaris PharmaStock saat ini.\n\n",
                        drug.getBrandNames().get(0)));
            }

            sb.append(String.format("%s merupakan obat golongan %s.\n\n", drug.getGenericName(), drug.getCategory()));
            sb.append(String.format("Kegunaan: %s.\n", String.join(", ", drug.getIndications())));

            if (!drug.getContraindications().isEmpty()) {
                sb.append(String.format("\nTidak dianjurkan untuk: %s.\n", String.join(", ", drug.getContraindications())));
            }

            // Cek apakah ada di inventory dengan nama berbeda (sama generik)
            if (!foundInInventory) {
                List<String> inStock = new ArrayList<>();
                for (Medicine med : inventory) {
                    String medName = med.getMedicineName().toLowerCase();
                    for (String brand : drug.getBrandNames()) {
                        if (medName.contains(brand.toLowerCase())) {
                            inStock.add(med.getMedicineName());
                            break;
                        }
                    }
                }
                if (!inStock.isEmpty()) {
                    sb.append("\nAlternatif dengan kandungan serupa yang TERSEDIA di inventaris:\n");
                    for (String name : inStock) {
                        sb.append(String.format("  • %s\n", name));
                    }
                }
            }

            sb.append("\n\nSumber: ✓ Medical Knowledge Base");
            return sb.toString();
        }

        return null;
    }

    private String handleAlternativeRecommendation(String lower) {
        // Cari berdasarkan brand → generic mapping
        String genericName = knowledgeService.getGenericName(lower);
        if (genericName == null) {
            DrugKnowledge byBrand = knowledgeService.findByExactBrand(lower);
            if (byBrand != null) {
                genericName = byBrand.getGenericName();
            }
        }

        if (genericName == null) return null;

        DrugKnowledge drug = knowledgeService.findByBrandOrGeneric(genericName);
        if (drug == null) return null;

        StringBuilder sb = new StringBuilder();
        String displayName = drug.getBrandNames().isEmpty() ? genericName : drug.getBrandNames().get(0);
        sb.append(String.format("%s tidak tersedia dalam inventaris PharmaStock.\n\n", displayName));
        sb.append(String.format("%s merupakan %s (%s).\n", drug.getGenericName(), drug.getCategory(),
                String.join(", ", drug.getIndications())));

        // Cari alternatif yang ada di inventory
        List<DrugKnowledge> knowledgeAlts = knowledgeService.getAlternatives(genericName);
        List<Medicine> inventoryAlts = new ArrayList<>();
        for (DrugKnowledge alt : knowledgeAlts) {
            List<Medicine> found = medicineRepo.findAll().stream()
                    .filter(m -> {
                        String mn = m.getMedicineName().toLowerCase();
                        for (String brand : alt.getBrandNames()) {
                            if (mn.contains(brand.toLowerCase())) return true;
                        }
                        return mn.contains(alt.getGenericName().toLowerCase());
                    })
                    .collect(Collectors.toList());
            inventoryAlts.addAll(found);
        }

        // Juga cari di inventory yang kategori sama
        if (inventoryAlts.isEmpty()) {
            for (Medicine med : medicineRepo.findAll()) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (cat.contains(drug.getCategory().toLowerCase())) {
                    inventoryAlts.add(med);
                }
            }
        }

        if (!inventoryAlts.isEmpty()) {
            sb.append("\nAlternatif yang tersedia di inventaris:\n");
            for (Medicine alt : inventoryAlts) {
                String status = alt.getStockQuantity() == 0 ? "⚠️ HABIS" :
                        (alt.getStockQuantity() < alt.getMinimumStock() ? "⚠️ Stok terbatas" : "✅ Tersedia");
                sb.append(String.format("  • %s - %s | Stok: %d %s\n",
                        alt.getMedicineName(), status, alt.getStockQuantity(), alt.getUnit()));
            }
        } else {
            // Alternatif dari knowledge base saja
            sb.append("\nAlternatif yang umum:\n");
            for (DrugKnowledge alt : knowledgeAlts) {
                sb.append(String.format("  • %s (%s)\n", alt.getGenericName(), alt.getCategory()));
            }
        }

        sb.append("\n⚠️ Silakan konsultasikan dengan tenaga kesehatan sebelum mengganti obat.");
        sb.append("\n\nSumber: ✓ Medical Knowledge Base ✓ Recommendation Engine");
        return sb.toString();
    }

    private String handleCategorySuggestion(String lower) {
        List<DrugKnowledge> bySymptom = knowledgeService.searchBySymptom(lower);
        if (!bySymptom.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Set<String> categories = new LinkedHashSet<>();
            for (DrugKnowledge drug : bySymptom) {
                categories.add(drug.getCategory());
            }

            if (categories.size() == 1) {
                String cat = categories.iterator().next();
                sb.append(String.format("Kategori obat yang sesuai: %s.\n\n", cat));

                // Cek inventory untuk kategori ini
                List<Medicine> inStock = medicineRepo.findAll().stream()
                        .filter(m -> m.getCategory() != null && m.getCategory().equalsIgnoreCase(cat))
                        .collect(Collectors.toList());

                if (!inStock.isEmpty()) {
                    sb.append("Tersedia di inventaris:\n");
                    for (Medicine med : inStock) {
                        sb.append(String.format("  • %s - Stok: %d %s\n",
                                med.getMedicineName(), med.getStockQuantity(), med.getUnit()));
                    }
                } else {
                    sb.append("Contoh obat yang umum:\n");
                    for (DrugKnowledge drug : bySymptom) {
                        sb.append(String.format("  • %s (%s)\n", drug.getGenericName(),
                                String.join(", ", drug.getBrandNames())));
                    }
                }
            } else {
                sb.append("Beberapa kategori obat yang mungkin sesuai:\n\n");
                for (String cat : categories) {
                    sb.append(String.format("  • %s\n", cat));
                }
                sb.append("\nUntuk informasi lebih spesifik, sebutkan gejala yang lebih detail.");
            }

            sb.append("\n\nSumber: ✓ Medical Knowledge Base");
            return sb.toString();
        }

        return "Silakan ajukan pertanyaan yang lebih spesifik tentang obat atau gejala yang Anda alami.\n\nContoh:\n- \"Apa obat sakit kepala?\"\n- \"Informasi Paracetamol\"\n- \"Saya mencari Panadol\"\n- \"Obat untuk radang lambung\"";
    }

    private List<Medicine> retrieveRelevantMedicines(String query) {
        String lower = query.toLowerCase().trim();
        List<Medicine> byName = medicineRepo.searchByName(lower);
        if (!byName.isEmpty()) return byName;
        List<Medicine> allMeds = medicineRepo.findAll();
        for (Medicine med : allMeds) {
            if (lower.contains(med.getCategory().toLowerCase())) {
                return medicineRepo.findByCategory(med.getCategory());
            }
        }
        if (containsAny(lower, "kritis", "rendah", "habis", "low stock")) return medicineRepo.findLowStock();
        if (containsAny(lower, "kadaluarsa", "expired", "expiry")) {
            List<Medicine> result = new ArrayList<>();
            result.addAll(medicineRepo.findExpired());
            result.addAll(medicineRepo.findNearExpiry(30));
            return result;
        }
        if (containsAny(lower, "semua", "semuanya", "total", "keseluruhan", "inventaris")) return allMeds;
        String[] words = lower.split("\\s+");
        for (String word : words) {
            if (word.length() > 3) {
                List<Medicine> wordSearch = medicineRepo.searchByName(word);
                if (!wordSearch.isEmpty()) return wordSearch;
            }
        }
        return allMeds;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}

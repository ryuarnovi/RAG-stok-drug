package com.pharmastock.service.ai;

import com.pharmastock.model.Medicine;
import com.pharmastock.repository.IMedicineRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Provider AI berbasis aturan lokal, tanpa memerlukan API eksternal.
 * Mengurai keyword dari pesan pengguna dan mengembalikan data langsung dari database.
 */
public class LocalRuleBasedProvider implements AIProvider {

    private final IMedicineRepository medicineRepo;

    public LocalRuleBasedProvider(IMedicineRepository medicineRepo) {
        this.medicineRepo = medicineRepo;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Silakan ajukan pertanyaan tentang inventaris apotek Anda.";
        }

        String lower = userMessage.toLowerCase().trim();

        // Cek stok obat tertentu
        if (containsAny(lower, "sisa", "stok", "berapa", "ada berapa", "jumlah stok")) {
            return handleStockQuery(lower);
        }

        // Cek kadaluarsa
        if (containsAny(lower, "kadaluarsa", "kadaluwarsa", "expired", "expiry", "tanggal kedaluwarsa")) {
            return handleExpiryQuery();
        }

        // Stok kritis
        if (containsAny(lower, "kritis", "low stock", "rendah", "habis", "menipis", "hampir habis")) {
            return handleLowStockQuery();
        }

        // Rekomendasi
        if (containsAny(lower, "rekomendasi", "saran", "suggest", "pesan", "order", "reorder")) {
            return handleRecommendation();
        }

        // Total inventory
        if (containsAny(lower, "total", "jumlah", "semua", "keseluruhan", "ringkasan", "summary")) {
            return handleTotalQuery();
        }

        // Info obat - cek apakah query menyebutkan nama obat
        String medicineInfo = findMedicineInfo(lower);
        if (medicineInfo != null) {
            return medicineInfo;
        }

        // Jika query mengandung kata terkait kesehatan/farmasi, cari rekomendasi
        if (isHealthQuery(lower)) {
            return suggestForCondition(lower);
        }

        // Default response - tampilkan inventaris
        return showAllMedicines();
    }

    private String showAllMedicines() {
        List<Medicine> allMeds = medicineRepo.findAll();
        if (allMeds.isEmpty()) {
            return "Tidak ada obat dalam inventaris saat ini.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Berikut %d obat yang tersedia di PharmaStock:\n\n", allMeds.size()));

        // Kelompokkan berdasarkan kategori
        String currentCategory = "";
        for (Medicine med : allMeds) {
            String category = med.getCategory() != null ? med.getCategory() : "Umum";
            if (!category.equals(currentCategory)) {
                currentCategory = category;
                sb.append(String.format("【%s】\n", currentCategory));
            }

            String status = med.getStockQuantity() == 0 ? "HABIS" :
                    (med.getStockQuantity() < med.getMinimumStock() ? "STOK KRITIS" : "Tersedia");

            sb.append(String.format("  • %s (%s) - Stok: %d %s [%s]",
                    med.getMedicineName(), med.getMedicineCode(),
                    med.getStockQuantity(), med.getUnit(), status));

            if (med.getSellingPrice() != null) {
                sb.append(String.format(" - Rp%,.0f", med.getSellingPrice().doubleValue()));
            }
            sb.append("\n");
        }

        sb.append("\nUntuk detail stok, ketik: \"sisa [nama obat]\"");
        sb.append("\nUntuk info lengkap, ketik nama obat (contoh: \"Paracetamol 500mg\")");

        return sb.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String findMedicineInfo(String query) {
        List<Medicine> allMeds = medicineRepo.findAll();
        for (Medicine med : allMeds) {
            String nameLower = med.getMedicineName().toLowerCase();
            String codeLower = med.getMedicineCode().toLowerCase();

            if (query.contains(nameLower) || query.contains(codeLower)) {
                StringBuilder sb = new StringBuilder();

                // Status obat
                String status;
                if (med.getStockQuantity() == 0) {
                    status = "HABIS";
                } else if (med.getStockQuantity() < med.getMinimumStock()) {
                    status = "STOK KRITIS";
                } else {
                    status = "AMAN";
                }

                sb.append(String.format("Informasi Obat: %s\n", med.getMedicineName()));
                sb.append("═══════════════════════════════════════════\n");
                sb.append(String.format("Kode         : %s\n", med.getMedicineCode()));
                sb.append(String.format("Nama         : %s\n", med.getMedicineName()));
                sb.append(String.format("Kategori     : %s\n", med.getCategory()));
                sb.append(String.format("Stok         : %d %s (Status: %s)\n",
                        med.getStockQuantity(), med.getUnit(), status));
                sb.append(String.format("Stok Minimum : %d %s\n", med.getMinimumStock(), med.getUnit()));
                sb.append(String.format("Harga Beli   : Rp %,.2f\n",
                        med.getPurchasePrice() != null ? med.getPurchasePrice().doubleValue() : 0.0));
                sb.append(String.format("Harga Jual   : Rp %,.2f\n",
                        med.getSellingPrice() != null ? med.getSellingPrice().doubleValue() : 0.0));

                if (med.getExpiryDate() != null) {
                    sb.append(String.format("Kadaluarsa   : %s\n", med.getExpiryDate()));
                    if (med.isExpired()) {
                        sb.append("[PERINGATAN] Obat ini telah KADALUARSA!\n");
                    }
                }

                if (med.getBatchNumber() != null) {
                    sb.append(String.format("Batch        : %s\n", med.getBatchNumber()));
                }

                if (med.getSupplierId() > 0) {
                    sb.append(String.format("Supplier ID  : %d\n", med.getSupplierId()));
                }

                // Rekomendasi berdasarkan status
                sb.append("\nRekomendasi: ");
                if (med.getStockQuantity() == 0) {
                    sb.append("Stok habis, segera lakukan pemesanan ulang!");
                } else if (med.isLowStock()) {
                    int reorderQty = (med.getMinimumStock() * 2) - med.getStockQuantity();
                    sb.append(String.format("Stok rendah, disarankan pesan %d %s.", reorderQty, med.getUnit()));
                } else if (med.isExpired()) {
                    sb.append("Obat kadaluarsa, segera lakukan retur atau pemusnahan.");
                } else {
                    sb.append("Stok dalam kondisi baik.");
                }

                return sb.toString();
            }
        }
        return null;
    }

    private String handleStockQuery(String query) {
        List<Medicine> allMeds = medicineRepo.findAll();
        for (Medicine med : allMeds) {
            if (query.contains(med.getMedicineName().toLowerCase()) ||
                    query.contains(med.getMedicineCode().toLowerCase())) {

                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Stok %s saat ini tersisa %d %s.\n\n",
                        med.getMedicineName(), med.getStockQuantity(), med.getUnit()));

                if (med.isLowStock()) {
                    sb.append(String.format("Ini berada di bawah ambang batas minimum (%d %s). ",
                            med.getMinimumStock(), med.getUnit()));
                    sb.append("Apakah Anda ingin memesan ulang?");
                } else if (med.isExpired()) {
                    sb.append("PERHATIAN: Obat ini telah kadaluarsa pada ")
                            .append(med.getExpiryDate()).append(".");
                } else {
                    sb.append("Stok dalam kondisi baik.");
                    if (med.getExpiryDate() != null) {
                        sb.append(" Kadaluarsa: ").append(med.getExpiryDate()).append(".");
                    }
                }
                return sb.toString();
            }
        }

        // Tidak ditemukan obat spesifik, tampilkan ringkasan
        int total = medicineRepo.count();
        int totalStock = medicineRepo.sumStockQuantity();
        return String.format("Total ada %d jenis obat dengan total stok %d unit di inventaris.\n" +
                "Sebutkan nama obat yang ingin Anda cek, misalnya: \"Berapa sisa Paracetamol 500mg?\"",
                total, totalStock);
    }

    private String handleExpiryQuery() {
        List<Medicine> expired = medicineRepo.findExpired();
        List<Medicine> nearExpiry = medicineRepo.findNearExpiry(30);

        StringBuilder sb = new StringBuilder();

        if (!expired.isEmpty()) {
            sb.append(String.format("Ditemukan %d obat yang telah kadaluarsa:\n\n", expired.size()));
            for (Medicine med : expired) {
                sb.append(String.format("- %s (Exp: %s, Stok: %d %s)\n",
                        med.getMedicineName(), med.getExpiryDate(), med.getStockQuantity(), med.getUnit()));
            }
        }

        if (!nearExpiry.isEmpty()) {
            sb.append(String.format("\nDitemukan %d obat yang akan kadaluarsa dalam 30 hari:\n\n", nearExpiry.size()));
            for (Medicine med : nearExpiry) {
                sb.append(String.format("- %s (Exp: %s, Stok: %d %s)\n",
                        med.getMedicineName(), med.getExpiryDate(), med.getStockQuantity(), med.getUnit()));
            }
        }

        if (expired.isEmpty() && nearExpiry.isEmpty()) {
            sb.append("Tidak ada obat yang kadaluarsa atau mendekati kadaluarsa saat ini.");
        }

        return sb.toString();
    }

    private String handleLowStockQuery() {
        List<Medicine> lowStock = medicineRepo.findLowStock();

        if (lowStock.isEmpty()) {
            return "Tidak ada obat dengan stok kritis saat ini. Semua stok dalam kondisi aman.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Ditemukan %d obat dengan stok kritis:\n\n", lowStock.size()));

        for (Medicine med : lowStock) {
            sb.append(String.format("- %s: %d %s (minimum: %d)\n",
                    med.getMedicineName(), med.getStockQuantity(), med.getUnit(), med.getMinimumStock()));
        }

        sb.append("\nDisarankan untuk segera melakukan pemesanan ulang.");
        return sb.toString();
    }

    private String handleRecommendation() {
        List<Medicine> lowStock = medicineRepo.findLowStock();
        List<Medicine> expired = medicineRepo.findExpired();

        StringBuilder sb = new StringBuilder("Rekomendasi PharmaStock:\n\n");

        if (!lowStock.isEmpty()) {
            sb.append("Pemesanan Ulang:\n");
            for (Medicine med : lowStock) {
                int reorderQty = (med.getMinimumStock() * 2) - med.getStockQuantity();
                sb.append(String.format("- %s: pesan %d %s\n",
                        med.getMedicineName(), reorderQty, med.getUnit()));
            }
        }

        if (!expired.isEmpty()) {
            sb.append("\nPerlu Tindakan (Kadaluarsa):\n");
            for (Medicine med : expired) {
                sb.append(String.format("- %s: retur/musnahkan %d %s\n",
                        med.getMedicineName(), med.getStockQuantity(), med.getUnit()));
            }
        }

        if (lowStock.isEmpty() && expired.isEmpty()) {
            sb.append("Semua stok dalam kondisi baik. Tidak ada tindakan mendesak yang diperlukan.");
        }

        return sb.toString();
    }

    private String handleTotalQuery() {
        int total = medicineRepo.count();
        int totalStock = medicineRepo.sumStockQuantity();
        int lowStock = medicineRepo.countLowStock();
        int expired = medicineRepo.countExpired();

        return String.format("Ringkasan Inventaris:\n\n" +
                "- Total Jenis Obat: %d\n" +
                "- Total Stok: %d unit\n" +
                "- Stok Rendah: %d jenis\n" +
                "- Kadaluarsa: %d jenis",
                total, totalStock, lowStock, expired);
    }

    private boolean isHealthQuery(String query) {
        return containsAny(query,
                "obat", "sakit", "radang", "demam", "batuk", "pilek", "nyeri",
                "sakit kepala", "maag", "diare", "alergi", "vitamin", "antibiotik",
                "pereda", "penyakit", "gejala", "terapi", "pengobatan",
                "kesehatan", "farmasi", "khasiat", "manfaat", "dosis",
                "tenggorokan", "panas", "flu", "infeksi", "luka", "gatal",
                "sariawan", "pusing", "mual", "demam", "sesak", "perut",
                "obati", "mengobati", "ampuh", "sembuh");
    }

    private String suggestForCondition(String query) {
        List<Medicine> allMeds = medicineRepo.findAll();
        if (allMeds.isEmpty()) {
            return "Tidak ada obat dalam inventaris saat ini.";
        }

        List<Medicine> matched = new ArrayList<>();
        String conditionName = "";
        String advice = "";

        // Deteksi kondisi dengan prioritas (lebih spesifik didahulukan)
        if (containsAny(query, "maag", "lambung", "asam lambung", "tukak", "gastrointestinal")) {
            conditionName = "radang lambung/maag";
            advice = "Untuk radang lambung, HINDARI obat golongan NSAID (seperti Ibuprofen). Disarankan obat yang melindungi lambung.";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "antasida", "lambung", "maag", "pencernaan", "gastrointestinal")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "sakit kepala", "pusing", "kepala pusing", "migrain", "kepala")) {
            conditionName = "sakit kepala";
            advice = "Untuk sakit kepala, berikut obat pereda nyeri yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "analgesik", "nyeri", "sakit kepala")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "demam", "panas", "meriang")) {
            conditionName = "demam";
            advice = "Untuk menurunkan demam, berikut obat yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "antipiretik", "analgesik", "demam")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "batuk", "pilek", "flu")) {
            conditionName = "batuk/pilek/flu";
            advice = "Untuk batuk dan pilek, berikut obat yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                String name = med.getMedicineName().toLowerCase();
                if (containsAny(cat, "batuk", "pilek", "flu", "antitusif", "ekspektoran")
                        || containsAny(name, "batuk", "flu")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "nyeri", "radang", "nyeri otot", "sakit gigi", "nyeri sendi")) {
            conditionName = "nyeri/radang";
            advice = "Berikut obat pereda nyeri dan anti-radang yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "analgesik", "anti-inflamasi", "antiinflamasi", "nyeri")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "alergi", "gatal", "biduran", "ruam")) {
            conditionName = "alergi";
            advice = "Untuk mengatasi alergi, berikut obat yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "alergi", "antihistamin")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "diare", "mencret")) {
            conditionName = "diare";
            advice = "Untuk mengatasi diare, berikut obat yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "diare", "pencernaan")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "luka", "lecet", "luka luar", "tergores")) {
            conditionName = "luka";
            advice = "Untuk perawatan luka, berikut yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "salep", "antiseptik", "luka", "topikal")) {
                    matched.add(med);
                }
            }
        } else if (containsAny(query, "vitamin", "suplemen", "daya tahan")) {
            conditionName = "vitamin/suplemen";
            advice = "Berikut vitamin dan suplemen yang tersedia:";
            for (Medicine med : allMeds) {
                String cat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";
                if (containsAny(cat, "vitamin", "suplemen", "multivitamin")) {
                    matched.add(med);
                }
            }
        }

        // Jika ada obat yang cocok dengan kondisi
        if (!matched.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(advice).append("\n\n");
            for (Medicine med : matched) {
                String status = med.getStockQuantity() == 0 ? "[HABIS]" :
                        (med.getStockQuantity() < med.getMinimumStock() ? "[Stok terbatas]" : "[Tersedia]");
                sb.append(String.format("• **%s** (%s) - %s\n",
                        med.getMedicineName(), med.getCategory(), status));
                if (med.getSellingPrice() != null) {
                    sb.append(String.format("  Harga: Rp%,.0f | Stok: %d %s\n",
                            med.getSellingPrice().doubleValue(), med.getStockQuantity(), med.getUnit()));
                }
            }
            sb.append(String.format("\nKetik \"%s\" untuk info detail obat.", matched.get(0).getMedicineName()));
            return sb.toString();
        }

        // Fallback: tidak ada kecocokan kategori
        StringBuilder sb = new StringBuilder();
        sb.append("Maaf, tidak ada obat yang cocok untuk kondisi tersebut di inventaris saat ini.\n\n");
        sb.append("Berikut daftar semua obat yang tersedia di PharmaStock:\n\n");
        String currentCategory = "";
        for (Medicine med : allMeds) {
            String cat = med.getCategory() != null ? med.getCategory() : "Umum";
            if (!cat.equals(currentCategory)) {
                currentCategory = cat;
                sb.append(String.format("【%s】\n", currentCategory));
            }
            sb.append(String.format("  • %s - Stok: %d %s\n",
                    med.getMedicineName(), med.getStockQuantity(), med.getUnit()));
        }
        sb.append("\nKetik nama obat untuk info lebih detail.");
        return sb.toString();
    }
}

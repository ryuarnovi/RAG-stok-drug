package com.pharmastock.service;

import com.pharmastock.model.DrugKnowledge;
import com.pharmastock.model.Medicine;
import com.pharmastock.repository.IMedicineRepository;

import java.util.*;
import java.util.stream.Collectors;

public class MedicalKnowledgeService {

    private final IMedicineRepository medicineRepo;
    private final Map<String, DrugKnowledge> drugDatabase = new HashMap<>();
    private final Map<String, String> brandToGeneric = new HashMap<>();
    private final Map<String, List<String>> categoryDrugs = new HashMap<>();
    private final Map<String, String> symptomCategory = new HashMap<>();

    public MedicalKnowledgeService(IMedicineRepository medicineRepo) {
        this.medicineRepo = medicineRepo;
        initDrugDatabase();
        initSymptomCategoryMap();
        initBrandToGenericMap();
    }

    private void initSymptomCategoryMap() {
        symptomCategory.put("sakit kepala", "Analgesik");
        symptomCategory.put("pusing", "Analgesik");
        symptomCategory.put("migrain", "Analgesik");
        symptomCategory.put("demam", "Antipiretik");
        symptomCategory.put("panas", "Antipiretik");
        symptomCategory.put("meriang", "Antipiretik");
        symptomCategory.put("batuk", "Antitusif");
        symptomCategory.put("pilek", "Dekongestan");
        symptomCategory.put("flu", "Antiviral");
        symptomCategory.put("nyeri", "Analgesik");
        symptomCategory.put("radang", "Anti-inflamasi");
        symptomCategory.put("lambung", "Antasida");
        symptomCategory.put("maag", "Antasida");
        symptomCategory.put("alergi", "Antihistamin");
        symptomCategory.put("gatal", "Antihistamin");
        symptomCategory.put("diare", "Antidiare");
        symptomCategory.put("luka", "Antiseptik");
        symptomCategory.put("infeksi", "Antibiotik");
        symptomCategory.put("vitamin", "Suplemen");
        symptomCategory.put("sakit gigi", "Analgesik");
        symptomCategory.put("sakit perut", "Antispasmodik");
        symptomCategory.put("mual", "Antiemetik");
        symptomCategory.put("tenggorokan", "Analgesik");
        symptomCategory.put("sariawan", "Antiseptik");
        symptomCategory.put("kurang darah", "Suplemen Besi");
        symptomCategory.put("tekanan darah", "Antihipertensi");
    }

    private void initBrandToGenericMap() {
        brandToGeneric.put("panadol", "Paracetamol");
        brandToGeneric.put("panadol extra", "Paracetamol + Kafein");
        brandToGeneric.put("sanmol", "Paracetamol");
        brandToGeneric.put("tempra", "Paracetamol");
        brandToGeneric.put("bodrex", "Paracetamol + Kafein");
        brandToGeneric.put("bodrex flu", "Paracetamol + Pseudoefedrin + Klorfeniramin");
        brandToGeneric.put("procold", "Paracetamol + Pseudoefedrin + Klorfeniramin");
        brandToGeneric.put("mixagrip", "Paracetamol + Pseudoefedrin + Klorfeniramin");
        brandToGeneric.put("decolgen", "Paracetamol + Pseudoefedrin + Klorfeniramin");
        brandToGeneric.put("paramex", "Paracetamol + Kafein");
        brandToGeneric.put("saridon", "Paracetamol + Propifenazon + Kafein");
        brandToGeneric.put("amoxsan", "Amoxicillin");
        brandToGeneric.put("amoxilin", "Amoxicillin");
        brandToGeneric.put("ibufen", "Ibuprofen");
        brandToGeneric.put("proris", "Ibuprofen");
        brandToGeneric.put("neurobion", "Vitamin B Complex");
        brandToGeneric.put("sangobion", "Suplemen Zat Besi");
        brandToGeneric.put("obh combi", "Ekspektoran + Antitusif");
        brandToGeneric.put("woods", "Antitusif");
        brandToGeneric.put("siladex", "Antitusif");
        brandToGeneric.put("konidin", "Dekstrometorfan + Klorfeniramin");
        brandToGeneric.put("komix", "Antitusif + Ekspektoran");
        brandToGeneric.put("inosine", "Inosin Pranobex");
        brandToGeneric.put("renovit", "Multivitamin");
        brandToGeneric.put("cavit", "Multivitamin");
        brandToGeneric.put("fatigon", "Multivitamin + Mineral");
        brandToGeneric.put("promag", "Antasida + Magnesium + Aluminium");
        brandToGeneric.put("mylanta", "Antasida + Simetikon");
        brandToGeneric.put("polysilane", "Antasida + Simetikon");
        brandToGeneric.put("ulen", "Omeprazole");
        brandToGeneric.put("omepran", "Omeprazole");
        brandToGeneric.put("lansox", "Lansoprazole");
        brandToGeneric.put("ranitidin", "Ranitidine");
        brandToGeneric.put("entrostop", "Attapulgite");
        brandToGeneric.put("diatabs", "Attapulgite");
        brandToGeneric.put("new diatabs", "Attapulgite");
        brandToGeneric.put("norit", "Karbon Aktif");
        brandToGeneric.put("ctm", "Klorfeniramin Maleat");
        brandToGeneric.put("cetirizine", "Cetirizine");
        brandToGeneric.put("zyrtec", "Cetirizine");
        brandToGeneric.put("betadine", "Povidone Iodine");
        brandToGeneric.put("salep 88", "Asam Salisilat + Sulfur");
        brandToGeneric.put("kalpanax", "Clotrimazole");
        brandToGeneric.put("voltaren", "Diklofenak");
        brandToGeneric.put("ponstan", "Asam Mefenamat");
        brandToGeneric.put("farsifen", "Ibuprofen");
        brandToGeneric.put("sanprima", "Kotrimoksazol");
        brandToGeneric.put("bactrim", "Kotrimoksazol");
    }

    private void initDrugDatabase() {
        // ========== ANALGESIK & ANTIPIRETIK ==========
        addDrug("Paracetamol", List.of("Panadol", "Sanmol", "Tempra", "Pamol"),
                "Analgesik", List.of("Nyeri ringan-sedang", "Demam", "Sakit kepala", "Sakit gigi"),
                List.of("Gagal hati berat", "Hipersensitivitas"), List.of(), 40);
        addDrug("Paracetamol + Kafein", List.of("Panadol Extra", "Bodrex", "Paramex"),
                "Analgesik", List.of("Sakit kepala tegang", "Migrain ringan", "Nyeri"),
                List.of("Gagal hati", "Hipersensitivitas"), List.of("Paracetamol"), 19);
        addDrug("Paracetamol + Pseudoefedrin + Klorfeniramin",
                List.of("Bodrex Flu", "Procold", "Mixagrip", "Decolgen"),
                "Flu", List.of("Flu", "Pilek", "Hidung tersumbat", "Demam", "Bersin"),
                List.of("Hipertensi berat", "Glaucoma", "Hipersensitivitas"),
                List.of("Paracetamol"), 27);
        addDrug("Ibuprofen", List.of("Ibuprofen", "Proris", "Farsifen"),
                "Anti-inflamasi", List.of("Nyeri sendi", "Radang", "Demam", "Nyeri otot", "Sakit gigi"),
                List.of("Tukak lambung", "Gagal ginjal", "Asma aspirin-sensitive", "Ibu hamil trimester 3"),
                List.of("Paracetamol"), 15);
        addDrug("Asam Mefenamat", List.of("Ponstan"),
                "Anti-inflamasi", List.of("Nyeri haid", "Nyeri gigi", "Nyeri otot", "Radang sendi"),
                List.of("Tukak lambung", "Gagal ginjal", "Asma", "Hipersensitivitas"),
                List.of("Paracetamol", "Ibuprofen"), 18);
        addDrug("Diklofenak", List.of("Voltaren", "Diklofenak"),
                "Anti-inflamasi", List.of("Radang sendi", "Nyeri otot", "Nyeri pasca operasi"),
                List.of("Tukak lambung", "Penyakit jantung", "Gagal ginjal"),
                List.of("Paracetamol", "Ibuprofen"), 22);

        // ========== ANTASIDA & LAMBUNG ==========
        addDrug("Antasida + Mg + Al", List.of("Promag"),
                "Antasida", List.of("Maag", "Asam lambung", "Nyeri lambung", "Kembung"),
                List.of("Hipersensitivitas", "Gagal ginjal berat"),
                List.of("Omeprazole"), 5);
        addDrug("Antasida + Simetikon", List.of("Mylanta", "Polysilane"),
                "Antasida", List.of("Maag", "Kembung", "Asam lambung", "Perut begah"),
                List.of("Hipersensitivitas", "Gagal ginjal"),
                List.of("Omeprazole"), 6);
        addDrug("Omeprazole", List.of("Ulen", "Omepran", "Omesec"),
                "Antasida", List.of("Tukak lambung", "GERD", "Asam lambung tinggi", "Maag kronis"),
                List.of("Hipersensitivitas", "Penggunaan >8 minggu tanpa konsultasi"),
                List.of("Promag", "Mylanta"), 10);

        // ========== BATUK & FLU ==========
        addDrug("Antitusif + Ekspektoran", List.of("OBH Combi", "Komix"),
                "Batuk", List.of("Batuk berdahak", "Batuk kering", "Flu"),
                List.of("Hipersensitivitas"), List.of("Woods", "Siladex"), 8);
        addDrug("Antitusif", List.of("Woods", "Siladex"),
                "Antitusif", List.of("Batuk kering", "Batuk tidak berdahak"),
                List.of("Batuk berdahak", "Asma", "Hipersensitivitas"),
                List.of("OBH Combi"), 9);

        // ========== ALERGI ==========
        addDrug("Cetirizine", List.of("Cetirizine", "Zyrtec"),
                "Antihistamin", List.of("Alergi", "Gatal-gatal", "Bersin", "Hidung meler"),
                List.of("Gagal ginjal berat", "Hipersensitivitas"),
                List.of("CTM"), 12);

        // ========== DIARE ==========
        addDrug("Attapulgite", List.of("Entrostop", "New Diatabs"),
                "Antidiare", List.of("Diare akut", "Mencret"),
                List.of("Demam tinggi", "Diare berdarah", "Hipersensitivitas"),
                List.of("Norit"), 7);
        addDrug("Oralit", List.of("Oralit"),
                "Antidiare", List.of("Dehidrasi akibat diare", "Muntaber"),
                List.of(), List.of(), 3);

        // ========== ANTIBIOTIK ==========
        addDrug("Amoxicillin", List.of("Amoxsan", "Amoxilin", "Amoxicillin"),
                "Antibiotik", List.of("Infeksi bakteri", "Infeksi saluran napas", "Infeksi telinga", "Infeksi saluran kemih"),
                List.of("Alergi penisilin", "Hipersensitivitas"),
                List.of("Kotrimoksazol"), 25);
        addDrug("Kotrimoksazol", List.of("Sanprima", "Bactrim"),
                "Antibiotik", List.of("Infeksi saluran napas", "Infeksi saluran kemih", "Infeksi saluran cerna"),
                List.of("Alergi sulfonamid", "Gagal hati", "Gagal ginjal"),
                List.of("Amoxicillin"), 24);

        // ========== VITAMIN & SUPLEMEN ==========
        addDrug("Vitamin B Complex", List.of("Neurobion"),
                "Vitamin", List.of("Kurang vitamin B", "Neuritis", "Kesemutan", "Mual saat hamil"),
                List.of("Hipersensitivitas"), List.of("Multivitamin"), 20);
        addDrug("Suplemen Zat Besi", List.of("Sangobion"),
                "Suplemen", List.of("Kurang darah", "Anemia", "Kelelahan"),
                List.of("Hemokromatosis", "Hipersensitivitas"),
                List.of("Multivitamin"), 21);
        addDrug("Multivitamin", List.of("Renovit", "Cavit", "Fatigon"),
                "Vitamin", List.of("Kurang vitamin", "Kelelahan", "Meningkatkan daya tahan tubuh"),
                List.of("Hipersensitivitas"), List.of(), 30);

        // ========== LUKA & KULIT ==========
        addDrug("Povidone Iodine", List.of("Betadine"),
                "Antiseptik", List.of("Luka lecet", "Luka sayat", "Infeksi kulit", "Desinfektan"),
                List.of("Alergi iodin", "Luka bakar luas", "Hipersensitivitas"),
                List.of("Salep 88"), 14);
        addDrug("Salep Asam Salisilat + Sulfur", List.of("Salep 88"),
                "Antijamur", List.of("Kurap", "Panu", "Eksim", "Gatal-gatal"),
                List.of("Luka terbuka", "Hipersensitivitas"),
                List.of("Betadine", "Kalpanax"), 13);
        addDrug("Clotrimazole", List.of("Kalpanax"),
                "Antijamur", List.of("Panu", "Kurap", "Kandidiasis kulit", "Infeksi jamur"),
                List.of("Hipersensitivitas"), List.of("Salep 88"), 16);

        // ========== LAINNYA ==========
        addDrug("Dekstrometorfan + Klorfeniramin", List.of("Konidin"),
                "Antitusif", List.of("Batuk kering", "Alergi", "Flu"),
                List.of("Asma", "Batuk berdahak", "Hipersensitivitas"),
                List.of("Woods", "OBH Combi"), 11);
    }

    private void addDrug(String genericName, List<String> brandNames, String category,
                         List<String> indications, List<String> contraindications,
                         List<String> alternatives, int id) {
        String desc = String.format(
                "%s (%s). Berkhasiat untuk: %s. Tidak dianjurkan untuk: %s.",
                genericName, category,
                String.join(", ", indications),
                contraindications.isEmpty() ? "-" : String.join(", ", contraindications));

        DrugKnowledge drug = new DrugKnowledge(
                genericName, brandNames, category,
                indications, contraindications, alternatives, desc);
        drugDatabase.put(genericName.toLowerCase(), drug);
        categoryDrugs.computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>()).add(genericName);
    }

    // ========== PUBLIC API ==========

    public DrugKnowledge findByBrandOrGeneric(String query) {
        String lower = query.toLowerCase().trim();
        // Cek brand name
        for (DrugKnowledge drug : drugDatabase.values()) {
            for (String brand : drug.getBrandNames()) {
                if (brand.toLowerCase().equals(lower) || brand.toLowerCase().contains(lower)) {
                    return drug;
                }
            }
        }
        // Cek generic name
        for (DrugKnowledge drug : drugDatabase.values()) {
            if (drug.getGenericName().toLowerCase().contains(lower)) {
                return drug;
            }
        }
        return null;
    }

    public List<DrugKnowledge> searchBySymptom(String query) {
        String lower = query.toLowerCase();
        Set<DrugKnowledge> results = new HashSet<>();

        // Cari berdasarkan symptom → category
        for (Map.Entry<String, String> entry : symptomCategory.entrySet()) {
            if (lower.contains(entry.getKey())) {
                List<DrugKnowledge> byCat = findByCategory(entry.getValue());
                results.addAll(byCat);
            }
        }

        // Cari langsung dari drug database (indications, brand, generic)
        for (DrugKnowledge drug : drugDatabase.values()) {
            if (drug.matchesQuery(lower)) {
                results.add(drug);
            }
        }

        return new ArrayList<>(results);
    }

    public List<DrugKnowledge> findByCategory(String category) {
        String lower = category.toLowerCase();
        List<DrugKnowledge> result = new ArrayList<>();
        for (DrugKnowledge drug : drugDatabase.values()) {
            if (drug.getCategory().toLowerCase().contains(lower)) {
                result.add(drug);
            }
        }
        return result;
    }

    public String getGenericName(String brandName) {
        String lower = brandName.toLowerCase().trim();
        for (Map.Entry<String, String> entry : brandToGeneric.entrySet()) {
            if (lower.equals(entry.getKey()) || lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public List<DrugKnowledge> getAlternatives(String genericName) {
        DrugKnowledge drug = drugDatabase.get(genericName.toLowerCase());
        if (drug == null) return List.of();

        List<DrugKnowledge> alternatives = new ArrayList<>();
        for (String alt : drug.getCommonAlternatives()) {
            DrugKnowledge altDrug = drugDatabase.get(alt.toLowerCase());
            if (altDrug != null) alternatives.add(altDrug);
        }
        // Tambah dari kategori yang sama
        for (DrugKnowledge d : drugDatabase.values()) {
            if (!d.getGenericName().equals(genericName)
                    && d.getCategory().equals(drug.getCategory())
                    && alternatives.size() < 5) {
                if (alternatives.stream().noneMatch(a -> a.getGenericName().equals(d.getGenericName()))) {
                    alternatives.add(d);
                }
            }
        }
        return alternatives;
    }

    public List<DrugKnowledge> findInventoryAlternatives(String query) {
        DrugKnowledge drug = findByBrandOrGeneric(query);
        String genericName = getGenericName(query.toLowerCase());

        if (drug == null && genericName != null) {
            drug = drugDatabase.get(genericName.toLowerCase());
        }

        if (drug == null) return List.of();

        List<DrugKnowledge> result = new ArrayList<>();
        List<Medicine> inventory = medicineRepo.findAll();

        for (Medicine med : inventory) {
            String medName = med.getMedicineName().toLowerCase();
            String medCat = med.getCategory() != null ? med.getCategory().toLowerCase() : "";

            // Cek apakah generic name cocok dengan inventory
            for (String brand : drug.getBrandNames()) {
                if (medName.contains(brand.toLowerCase())) {
                    result.add(drug);
                    break;
                }
            }
            // Cek kategori sama
            DrugKnowledge finalDrug = drug;
            if (medCat.contains(finalDrug.getCategory().toLowerCase())) {
                if (result.stream().noneMatch(r -> r.getGenericName().equals(finalDrug.getGenericName()))) {
                    boolean foundInInv = false;
                    for (Medicine m : inventory) {
                        if (m.getMedicineName().toLowerCase().contains(medName)) {
                            foundInInv = true;
                            break;
                        }
                    }
                    if (!foundInInv) {
                        DrugKnowledge invDrug = drugDatabase.get(med.getMedicineName().toLowerCase());
                        if (invDrug != null && !invDrug.getGenericName().equals(drug.getGenericName())) {
                            result.add(invDrug);
                        }
                    }
                }
            }
        }

        return result;
    }

    public DrugKnowledge findByExactBrand(String brandName) {
        String lower = brandName.toLowerCase().trim();
        for (DrugKnowledge drug : drugDatabase.values()) {
            for (String brand : drug.getBrandNames()) {
                if (brand.toLowerCase().equals(lower)) {
                    return drug;
                }
            }
        }
        return null;
    }

    public List<String> getSymptomSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        String lower = query.toLowerCase();
        for (String symptom : symptomCategory.keySet()) {
            if (symptom.contains(lower) || lower.contains(symptom)) {
                suggestions.add(symptom);
            }
        }
        return suggestions;
    }
}

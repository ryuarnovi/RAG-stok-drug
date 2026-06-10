package com.pharmastock.model;

import java.util.List;

public class DrugKnowledge {

    private String genericName;
    private List<String> brandNames;
    private String category;
    private List<String> indications;
    private List<String> contraindications;
    private List<String> commonAlternatives;
    private String description;
    private String dosageForm;

    public DrugKnowledge(String genericName, List<String> brandNames, String category,
                         List<String> indications, List<String> contraindications,
                         List<String> commonAlternatives, String description) {
        this.genericName = genericName;
        this.brandNames = brandNames;
        this.category = category;
        this.indications = indications;
        this.contraindications = contraindications;
        this.commonAlternatives = commonAlternatives;
        this.description = description;
    }

    public String getGenericName() { return genericName; }
    public List<String> getBrandNames() { return brandNames; }
    public String getCategory() { return category; }
    public List<String> getIndications() { return indications; }
    public List<String> getContraindications() { return contraindications; }
    public List<String> getCommonAlternatives() { return commonAlternatives; }
    public String getDescription() { return description; }

    public boolean matchesQuery(String query) {
        String lower = query.toLowerCase();
        if (genericName.toLowerCase().contains(lower)) return true;
        if (category.toLowerCase().contains(lower)) return true;
        for (String brand : brandNames) {
            if (brand.toLowerCase().contains(lower)) return true;
        }
        for (String indication : indications) {
            if (indication.toLowerCase().contains(lower)) return true;
        }
        return false;
    }
}

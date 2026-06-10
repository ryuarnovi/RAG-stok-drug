package com.pharmastock.repository;

import com.pharmastock.model.Supplier;
import java.util.List;

public interface ISupplierRepository extends BaseRepository<Supplier> {
    List<Supplier> searchByName(String query);
    int countMedicines(int supplierId);
}

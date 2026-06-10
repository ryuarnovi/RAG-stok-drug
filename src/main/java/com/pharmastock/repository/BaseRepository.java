package com.pharmastock.repository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T> {
    List<T> findAll();
    Optional<T> findById(int id);
    int save(T entity);
    boolean update(T entity);
    boolean delete(int id);
}

package com.pharmastock.repository;

import com.pharmastock.config.DatabaseConfig;

public abstract class AbstractRepository<T> implements BaseRepository<T> {
    protected final DatabaseConfig db;

    protected AbstractRepository(DatabaseConfig db) {
        this.db = db;
    }
}

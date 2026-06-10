package com.pharmastock.repository;

import com.pharmastock.model.User;
import java.util.Optional;

public interface IUserRepository extends BaseRepository<User> {
    Optional<User> findByUsername(String username);
}

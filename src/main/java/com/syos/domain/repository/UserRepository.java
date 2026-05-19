package com.syos.domain.repository;

import java.util.Optional;

import com.syos.domain.entity.User;

public interface UserRepository {
    void save(User user);

    Optional<User> findByUsername(String username);
}
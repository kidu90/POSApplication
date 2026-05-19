package com.syos.application.usecase;

import com.syos.domain.entity.User;
import com.syos.domain.repository.UserRepository;

public class RegisterUseCase {
    private final UserRepository userRepository;

    public RegisterUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(String fullName, String address, String username, String password) {
        if (fullName == null || fullName.isBlank() || address == null || address.isBlank()
            || username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("All registration fields are required");
        }

        String normalizedUsername = username.trim();
        if (userRepository.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User(normalizedUsername, password, fullName, address);
        userRepository.save(user);
        return userRepository.findByUsername(normalizedUsername).orElse(user);
    }
}
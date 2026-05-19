package com.syos.domain.entity;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String address;

    public User(Long id, String username, String password, String fullName, String address) {
        this.id = id;
        this.username = requireText(username, "Username cannot be null or empty");
        this.password = requireText(password, "Password cannot be null or empty");
        this.fullName = requireText(fullName, "Full name cannot be null or empty");
        this.address = requireText(address, "Address cannot be null or empty");
    }

    public User(String username, String password, String fullName, String address) {
        this(null, username, password, fullName, address);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
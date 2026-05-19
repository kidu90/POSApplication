package com.syos.unit.application.usecase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.application.usecase.LoginUseCase;
import com.syos.domain.entity.User;
import com.syos.domain.repository.UserRepository;

/**
 * Verifies login validation because the server depends on deterministic authentication outcomes.
 */
class LoginUseCaseTest {
    private InMemoryUserRepository repository;
    private LoginUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        repository.save(new User("alice", "password", "Alice Example", "1 Test Lane"));
        useCase = new LoginUseCase(repository);
    }

    @Test
    void shouldReturnUser_whenCredentialsAreValid() {
        User user = useCase.execute("alice", "password");

        assertAll(
            () -> assertEquals("alice", user.getUsername()),
            () -> assertEquals("Alice Example", user.getFullName())
        );
    }

    @Test
    void shouldThrow_whenPasswordIsWrong() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("alice", "PASSWORD"));
    }

    @Test
    void shouldThrow_whenUsernameDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("missing", "password"));
    }

    @Test
    void shouldThrow_whenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(null, "password"));
    }

    @Test
    void shouldThrow_whenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("alice", null));
    }

    @Test
    void shouldComparePasswordsCaseSensitively() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute("alice", "Password"));
    }

    private static class InMemoryUserRepository implements UserRepository {
        private final Map<String, User> users = new HashMap<>();

        @Override
        public void save(User user) {
            users.put(user.getUsername(), user);
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return Optional.ofNullable(users.get(username));
        }
    }
}

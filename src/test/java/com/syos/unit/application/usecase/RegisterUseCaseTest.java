package com.syos.unit.application.usecase;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.syos.application.usecase.RegisterUseCase;
import com.syos.domain.entity.User;
import com.syos.domain.repository.UserRepository;

/**
 * Verifies registration validation and persistence because the GUI and server depend on safe user creation.
 */
class RegisterUseCaseTest {
    private InMemoryUserRepository repository;
    private RegisterUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        useCase = new RegisterUseCase(repository);
    }

    @Test
    void shouldSaveUser_whenUsernameIsNew() {
        User user = useCase.execute("Alice Example", "1 Test Lane", "alice", "password");

        assertAll(
            () -> assertEquals("alice", user.getUsername()),
            () -> assertEquals(user, repository.findByUsername("alice").orElseThrow())
        );
    }

    @Test
    void shouldThrow_whenUsernameIsDuplicate() {
        repository.save(new User("alice", "password", "Alice Example", "1 Test Lane"));

        assertThrows(IllegalArgumentException.class,
            () -> useCase.execute("Alice Example", "1 Test Lane", "alice", "password"));
    }

    @Test
    void shouldThrow_whenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> useCase.execute("Alice Example", "1 Test Lane", null, "password"));
    }

    @Test
    void shouldThrow_whenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class,
            () -> useCase.execute("Alice Example", "1 Test Lane", "   ", "password"));
    }

    @Test
    void shouldThrow_whenPasswordIsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> useCase.execute("Alice Example", "1 Test Lane", "alice", null));
    }

    @Test
    void shouldAllowRepositoryLookupAfterSuccessfulRegistration() {
        useCase.execute("Alice Example", "1 Test Lane", "alice", "password");

        assertEquals("alice", repository.findByUsername("alice").orElseThrow().getUsername());
    }

    @Test
    void shouldStorePasswordAsProvided_whenRegistrationSucceeds() {
        User user = useCase.execute("Alice Example", "1 Test Lane", "alice", "password");

        assertNotEquals("", user.getPassword());
        assertEquals("password", repository.findByUsername("alice").orElseThrow().getPassword());
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

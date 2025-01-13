package at.htlleonding;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.util.Arrays;
import java.util.UUID;

@ApplicationScoped
@Slf4j
@Transactional
public class LoginService {
    public static class Argon2Singleton {
        private static class Holder {
            private static final Argon2 INSTANCE = Argon2Factory.create();
        }
        private Argon2Singleton() {}
        public static Argon2 getInstance() {
            return Holder.INSTANCE;
        }
    }
    @Inject
    LoginPanacheRepository loginRepo;

    public User getUserById(UUID id) {
        log.info("Getting user by id: {}", id);
        return loginRepo.findById(id);
    }

    public void addUser(User user) {
        log.info("Adding user: {}", user.getUsername());
        checkArguments(user);

        if (loginRepo.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists!");
        }
        user.setPassword(encryptPassword(user.getPassword()));
        loginRepo.persist(user);
    }

    static String encryptPassword(String password) {
        password += Dotenv.load().get("PEPPER");
        Argon2 argon2 = Argon2Singleton.getInstance();
        return argon2.hash(2, 65536, 1, password.toCharArray()); // The generated hash includes the salt automatically
    }

    public boolean checkPassword(String username, String password) {
        log.info("Checking password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException();
        }
        Argon2 argon2 = Argon2Singleton.getInstance();
        password += Dotenv.load().get("PEPPER");
        return argon2.verify(user.getPassword(), password.toCharArray());
    }

    public void resetPassword(String username) {
        log.info("Resetting password for user: {}", username);
        User user = loginRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        log.info("EMAIL SENDING TO: {}", user.getUsername());
        log.info("Email: click this Link to enter a new password");
        log.info("Enter new password: ");
        user.setPassword(encryptPassword(Arrays.toString(System.console().readPassword())));
    }

    public void updateUser(User user) {
        log.info("Updating user: {}", user.getId());
        checkArguments(user);
        loginRepo.updateUser(user);
    }

    private void checkArguments(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password must not be empty");
        }

        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }

        if (user.getTelephoneNumber() == null || user.getTelephoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Telephone Number must not be empty");
        }
    }

    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);
        User user = loginRepo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found!");
        }
        loginRepo.deleteUser(user);
    }

    public void deleteUserByName(String username) {
        log.info("Deleting user: {}", username);
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty");
        }
        loginRepo.deleteUserByName(username);
    }
}

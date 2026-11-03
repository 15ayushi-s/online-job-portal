package com.jobhub.service;

import com.jobhub.dao.UserDao;
import com.jobhub.exception.AuthException;
import com.jobhub.exception.ConflictException;
import com.jobhub.exception.ForbiddenException;
import com.jobhub.exception.NotFoundException;
import com.jobhub.exception.ValidationException;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.util.LoginThrottle;
import com.jobhub.util.Passwords;
import com.jobhub.util.Validation;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Business rules for accounts. Knows nothing about HTTP or SQL. */
public class UserService {
    private final UserDao users;
    private final LoginThrottle throttle = new LoginThrottle();

    public UserService(UserDao users) { this.users = users; }

    public User register(String name, String email, String password, String roleRaw) {
        name = Validation.required(name, "Name", 255);
        email = Validation.email(email);
        if (password == null || password.length() < 6) throw new ValidationException("Password must be at least 6 characters");
        if (password.length() > 128) throw new ValidationException("Password is too long");
        Role role = (roleRaw == null || roleRaw.isBlank()) ? Role.JOB_SEEKER : Role.parse(roleRaw);
        if (role == Role.ADMIN) throw new ValidationException("Role must be JOB_SEEKER or EMPLOYER");
        if (users.findByEmail(email).isPresent()) throw new ConflictException("Email already registered");
        return users.save(User.create(null, name, email, Passwords.hash(password), role));
    }

    public User login(String email, String password) {
        if (email == null || password == null) throw new AuthException("Invalid email or password");
        String key = email.trim().toLowerCase(Locale.ROOT);
        throttle.checkAllowed(key);
        Optional<User> found = users.findByEmail(key);
        if (found.isEmpty() || !Passwords.verify(password, found.get().getPasswordHash())) {
            throttle.recordFailure(key);
            throw new AuthException("Invalid email or password");
        }
        throttle.reset(key);
        return found.get();
    }

    public List<User> listAll(User actor) {
        if (!actor.isAdmin()) throw new ForbiddenException("Admins only");
        return users.findAll();
    }

    public User update(User actor, Long id, String name, String email) {
        if (!actor.isAdmin() && !actor.getId().equals(id)) throw new ForbiddenException("You can only edit your own profile");
        User u = users.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        String newName = Validation.required(name, "Name", 255);
        String newEmail = Validation.email(email);
        Optional<User> clash = users.findByEmail(newEmail);
        if (clash.isPresent() && !clash.get().getId().equals(id)) throw new ConflictException("Email already registered");
        u.setName(newName);
        u.setEmail(newEmail);
        return users.save(u);
    }
}

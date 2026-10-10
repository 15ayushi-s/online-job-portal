package com.jobhub.dao.jdbc;

import com.jobhub.dao.UserDao;
import com.jobhub.exception.ConflictException;
import com.jobhub.exception.DataAccessException;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private static final String COLUMNS = "id, name, email, password_hash, role";

    private static User map(ResultSet rs) throws SQLException {
        return User.create(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                rs.getString("password_hash"), Role.valueOf(rs.getString("role")));
    }

    private Optional<User> findOne(String where, Object param) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE " + where;
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) { return findOne("id = ?", id); }

    @Override
    public Optional<User> findByEmail(String email) { return findOne("email = ?", email); }

    @Override
    public List<User> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list users", e);
        }
    }

    @Override
    public User save(User u) {
        try (Connection c = Db.get()) {
            if (u.getId() == null) {
                String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, u.getName());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPasswordHash());
                    ps.setString(4, u.getRole().name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) u.setId(keys.getLong(1));
                    }
                }
            } else {
                // role is intentionally not updatable
                String sql = "UPDATE users SET name = ?, email = ?, password_hash = ? WHERE id = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, u.getName());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPasswordHash());
                    ps.setLong(4, u.getId());
                    ps.executeUpdate();
                }
            }
            return u;
        } catch (SQLException e) {
            if (Db.isDuplicate(e)) throw new ConflictException("Email already registered");
            throw new DataAccessException("Could not save user", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete user", e);
        }
    }
}

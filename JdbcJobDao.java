package com.jobhub.dao.jdbc;

import com.jobhub.dao.JobDao;
import com.jobhub.exception.DataAccessException;
import com.jobhub.exception.ValidationException;
import com.jobhub.model.Job;
import com.jobhub.model.JobStatus;
import com.jobhub.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class JdbcJobDao implements JobDao {
    private static final String COLUMNS =
        "id, title, company, location, type, salary, description, skills, status, employer_id, created_at";
    private static final String ORDER = " ORDER BY created_at DESC, id DESC";

    private static Job map(ResultSet rs) throws SQLException {
        Job j = new Job();
        j.setId(rs.getLong("id"));
        j.setTitle(rs.getString("title"));
        j.setCompany(rs.getString("company"));
        j.setLocation(rs.getString("location"));
        j.setType(rs.getString("type"));
        j.setSalary(rs.getString("salary"));
        j.setDescription(rs.getString("description"));
        j.setSkills(rs.getString("skills"));
        j.setStatus(JobStatus.valueOf(rs.getString("status")));
        j.setEmployerId(rs.getLong("employer_id"));
        j.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return j;
    }

    private List<Job> query(String sql, Object... params) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<Job> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read jobs", e);
        }
    }

    @Override
    public Optional<Job> findById(Long id) {
        return query("SELECT " + COLUMNS + " FROM jobs WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Job> findAll() { return query("SELECT " + COLUMNS + " FROM jobs" + ORDER); }

    @Override
    public List<Job> findByEmployerId(Long employerId) {
        return query("SELECT " + COLUMNS + " FROM jobs WHERE employer_id = ?" + ORDER, employerId);
    }

    @Override
    public List<Job> searchApproved(String q) {
        String base = "SELECT " + COLUMNS + " FROM jobs WHERE status = 'APPROVED'";
        if (q == null || q.isBlank()) return query(base + ORDER);
        // escape LIKE wildcards typed by the user
        String like = "%" + q.trim().toLowerCase(Locale.ROOT)
            .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return query(base + " AND (LOWER(title) LIKE ? OR LOWER(company) LIKE ? OR LOWER(location) LIKE ?"
            + " OR LOWER(skills) LIKE ?)" + ORDER, like, like, like, like);
    }

    @Override
    public Job save(Job j) {
        try (Connection c = Db.get()) {
            if (j.getId() == null) {
                String sql = "INSERT INTO jobs (title, company, location, type, salary, description, skills,"
                    + " status, employer_id, created_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, j.getTitle());
                    ps.setString(2, j.getCompany());
                    ps.setString(3, j.getLocation());
                    ps.setString(4, j.getType());
                    ps.setString(5, j.getSalary());
                    ps.setString(6, j.getDescription());
                    ps.setString(7, j.getSkills());
                    ps.setString(8, j.getStatus().name());
                    ps.setLong(9, j.getEmployerId());
                    ps.setTimestamp(10, Timestamp.valueOf(j.getCreatedAt()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) j.setId(keys.getLong(1));
                    }
                }
            } else {
                String sql = "UPDATE jobs SET title=?, company=?, location=?, type=?, salary=?, description=?,"
                    + " skills=?, status=? WHERE id=?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, j.getTitle());
                    ps.setString(2, j.getCompany());
                    ps.setString(3, j.getLocation());
                    ps.setString(4, j.getType());
                    ps.setString(5, j.getSalary());
                    ps.setString(6, j.getDescription());
                    ps.setString(7, j.getSkills());
                    ps.setString(8, j.getStatus().name());
                    ps.setLong(9, j.getId());
                    ps.executeUpdate();
                }
            }
            return j;
        } catch (SQLException e) {
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException)
                throw new ValidationException("Employer does not exist");
            throw new DataAccessException("Could not save job", e);
        }
    }

    /** Deletes the job and its applications in ONE transaction (all or nothing). */
    @Override
    public boolean deleteById(Long id) {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement apps = c.prepareStatement("DELETE FROM applications WHERE job_id = ?");
                 PreparedStatement job = c.prepareStatement("DELETE FROM jobs WHERE id = ?")) {
                apps.setLong(1, id);
                apps.executeUpdate();
                job.setLong(1, id);
                int rows = job.executeUpdate();
                c.commit();
                return rows > 0;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete job", e);
        }
    }
}

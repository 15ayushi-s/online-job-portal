package com.jobhub.dao.jdbc;

import com.jobhub.dao.ApplicationDao;
import com.jobhub.exception.ConflictException;
import com.jobhub.exception.DataAccessException;
import com.jobhub.exception.ValidationException;
import com.jobhub.model.Application;
import com.jobhub.model.ApplicationStatus;
import com.jobhub.model.ApplicationView;
import com.jobhub.util.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcApplicationDao implements ApplicationDao {
    private static final String VIEW_SQL =
        "SELECT a.id, a.job_id, j.title, j.company, a.seeker_id, u.name, u.email, a.status, a.resume_url, a.applied_at"
        + " FROM applications a JOIN jobs j ON j.id = a.job_id JOIN users u ON u.id = a.seeker_id";
    private static final String VIEW_ORDER = " ORDER BY a.applied_at DESC, a.id DESC";

    private static Application mapEntity(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getLong("id"));
        a.setJobId(rs.getLong("job_id"));
        a.setSeekerId(rs.getLong("seeker_id"));
        a.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        a.setResumeUrl(rs.getString("resume_url"));
        a.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime());
        return a;
    }

    private static ApplicationView mapView(ResultSet rs) throws SQLException {
        return new ApplicationView(rs.getLong("id"), rs.getLong("job_id"), rs.getString("title"),
            rs.getString("company"), rs.getLong("seeker_id"), rs.getString("name"), rs.getString("email"),
            ApplicationStatus.valueOf(rs.getString("status")), rs.getString("resume_url"),
            rs.getTimestamp("applied_at").toLocalDateTime());
    }

    private List<ApplicationView> views(String where, Long id) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(VIEW_SQL + where + VIEW_ORDER)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<ApplicationView> list = new ArrayList<>();
                while (rs.next()) list.add(mapView(rs));
                return list;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read applications", e);
        }
    }

    @Override
    public List<ApplicationView> findViewsBySeeker(Long seekerId) { return views(" WHERE a.seeker_id = ?", seekerId); }

    @Override
    public List<ApplicationView> findViewsByJob(Long jobId) { return views(" WHERE a.job_id = ?", jobId); }

    @Override
    public boolean exists(Long jobId, Long seekerId) {
        String sql = "SELECT 1 FROM applications WHERE job_id = ? AND seeker_id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.setLong(2, seekerId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check application", e);
        }
    }

    @Override
    public Optional<Application> findById(Long id) {
        String sql = "SELECT id, job_id, seeker_id, status, resume_url, applied_at FROM applications WHERE id = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapEntity(rs)) : Optional.empty(); }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read application", e);
        }
    }

    @Override
    public List<Application> findAll() {
        String sql = "SELECT id, job_id, seeker_id, status, resume_url, applied_at FROM applications ORDER BY id";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Application> list = new ArrayList<>();
            while (rs.next()) list.add(mapEntity(rs));
            return list;
        } catch (SQLException e) {
            throw new DataAccessException("Could not list applications", e);
        }
    }

    @Override
    public Application save(Application a) {
        try (Connection c = Db.get()) {
            if (a.getId() == null) {
                String sql = "INSERT INTO applications (job_id, seeker_id, status, resume_url, applied_at) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, a.getJobId());
                    ps.setLong(2, a.getSeekerId());
                    ps.setString(3, a.getStatus().name());
                    ps.setString(4, a.getResumeUrl());
                    ps.setTimestamp(5, Timestamp.valueOf(a.getAppliedAt()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) a.setId(keys.getLong(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement("UPDATE applications SET status = ?, resume_url = ? WHERE id = ?")) {
                    ps.setString(1, a.getStatus().name());
                    ps.setString(2, a.getResumeUrl());
                    ps.setLong(3, a.getId());
                    ps.executeUpdate();
                }
            }
            return a;
        } catch (SQLException e) {
            if (Db.isDuplicate(e)) throw new ConflictException("You have already applied to this job");
            if (e instanceof SQLIntegrityConstraintViolationException) throw new ValidationException("Job or user does not exist");
            throw new DataAccessException("Could not save application", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM applications WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete application", e);
        }
    }
}

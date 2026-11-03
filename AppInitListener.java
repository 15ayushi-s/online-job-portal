package com.jobhub.web;

import com.jobhub.dao.jdbc.JdbcJobDao;
import com.jobhub.dao.jdbc.JdbcUserDao;
import com.jobhub.model.Job;
import com.jobhub.model.JobStatus;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.service.AppContext;
import com.jobhub.util.Db;
import com.jobhub.util.Passwords;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

/** Runs once when the web app starts: creates the tables (schema.sql) and the demo data. */
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            createSchema();
            seed();
            sce.getServletContext().log("JobHub: database ready");
        } catch (RuntimeException e) {
            sce.getServletContext().log("JobHub: DATABASE SETUP FAILED - " + e.getMessage(), e);
            throw e;   // fail the deployment loudly instead of serving a broken app
        }
    }

    private void createSchema() {
        String script;
        try (InputStream in = getClass().getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not found on classpath");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                script = r.lines().filter(l -> !l.trim().startsWith("--")).collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read schema.sql", e);
        }
        try (Connection c = Db.get(); Statement st = c.createStatement()) {
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) st.execute(sql);
            }
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Cannot create tables: " + e.getMessage(), e);
        }
    }

    private void seed() {
        JdbcUserDao users = AppContext.userDao();
        JdbcJobDao jobs = AppContext.jobDao();
        boolean firstRun = users.findByEmail("employer@jobhub.com").isEmpty();   // seed sample jobs only once
        demoUser(users, "Admin", "admin@jobhub.com", "admin123", Role.ADMIN);
        User employer = demoUser(users, "Demo Employer", "employer@jobhub.com", "employer123", Role.EMPLOYER);
        demoUser(users, "Demo Seeker", "seeker@jobhub.com", "seeker123", Role.JOB_SEEKER);

        if (firstRun) {
            sampleJob(jobs, employer, "Java Backend Developer", "Acme Tech", "Bengaluru", "Full Time", "8-12 LPA",
                "Java, Servlets, JDBC, MySQL", "Build and maintain REST services for our hiring platform.");
            sampleJob(jobs, employer, "React Frontend Intern", "Acme Tech", "Remote", "Internship", "20,000 / month",
                "React, JavaScript, CSS", "Work with our design team to build responsive interfaces.");
            sampleJob(jobs, employer, "QA Engineer", "Acme Tech", "Delhi", "Full Time", "6-9 LPA",
                "Testing, SQL, Selenium", "Own test plans and automation for new releases.");
        }
    }

    private User demoUser(JdbcUserDao users, String name, String email, String password, Role role) {
        return users.findByEmail(email).orElseGet(
            () -> users.save(User.create(null, name, email, Passwords.hash(password), role)));
    }

    private void sampleJob(JdbcJobDao jobs, User employer, String title, String company, String location,
                           String type, String salary, String skills, String description) {
        Job j = new Job();
        j.setTitle(title); j.setCompany(company); j.setLocation(location); j.setType(type);
        j.setSalary(salary); j.setSkills(skills); j.setDescription(description);
        j.setEmployerId(employer.getId());
        j.setStatus(JobStatus.APPROVED);
        jobs.save(j);
    }
}

package com.jobhub.service;

import com.jobhub.dao.jdbc.JdbcApplicationDao;
import com.jobhub.dao.jdbc.JdbcJobDao;
import com.jobhub.dao.jdbc.JdbcUserDao;

/** Wires DAOs into services once (simple hand-written dependency injection). */
public final class AppContext {
    private static final JdbcUserDao USER_DAO = new JdbcUserDao();
    private static final JdbcJobDao JOB_DAO = new JdbcJobDao();
    private static final JdbcApplicationDao APPLICATION_DAO = new JdbcApplicationDao();

    private static final UserService USERS = new UserService(USER_DAO);
    private static final JobService JOBS = new JobService(JOB_DAO);
    private static final ApplicationService APPLICATIONS = new ApplicationService(APPLICATION_DAO, JOB_DAO);

    private AppContext() {}

    public static UserService users() { return USERS; }
    public static JobService jobs() { return JOBS; }
    public static ApplicationService applications() { return APPLICATIONS; }
    public static JdbcUserDao userDao() { return USER_DAO; }
    public static JdbcJobDao jobDao() { return JOB_DAO; }
}

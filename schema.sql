-- JobHub Portal schema (MySQL 8, InnoDB).
-- Executed automatically at application start-up (AppInitListener) and safe to re-run.
-- The database itself is created by the JDBC URL (createDatabaseIfNotExist=true).

CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','EMPLOYER','JOB_SEEKER') NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS jobs (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    title       VARCHAR(255)  NOT NULL,
    company     VARCHAR(255)  NOT NULL,
    location    VARCHAR(255)  NOT NULL,
    type        VARCHAR(50),
    salary      VARCHAR(255),
    description VARCHAR(2000),
    skills      VARCHAR(255),
    status      ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    employer_id BIGINT        NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_jobs_status (status),
    CONSTRAINT fk_jobs_employer FOREIGN KEY (employer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    job_id     BIGINT       NOT NULL,
    seeker_id  BIGINT       NOT NULL,
    status     ENUM('APPLIED','SHORTLISTED','REJECTED','HIRED') NOT NULL DEFAULT 'APPLIED',
    resume_url VARCHAR(255),
    applied_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_application (job_id, seeker_id),
    CONSTRAINT fk_app_job    FOREIGN KEY (job_id)    REFERENCES jobs  (id) ON DELETE CASCADE,
    CONSTRAINT fk_app_seeker FOREIGN KEY (seeker_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

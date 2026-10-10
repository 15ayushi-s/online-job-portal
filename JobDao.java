package com.jobhub.dao;

import com.jobhub.model.Job;
import java.util.List;

public interface JobDao extends Dao<Job, Long> {
    /** Approved jobs only; a null/blank query returns all of them. */
    List<Job> searchApproved(String query);
    List<Job> findByEmployerId(Long employerId);
}

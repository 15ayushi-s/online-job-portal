package com.jobhub.dao;

import com.jobhub.model.Application;
import com.jobhub.model.ApplicationView;
import java.util.List;

public interface ApplicationDao extends Dao<Application, Long> {
    boolean exists(Long jobId, Long seekerId);
    List<ApplicationView> findViewsBySeeker(Long seekerId);
    List<ApplicationView> findViewsByJob(Long jobId);
}

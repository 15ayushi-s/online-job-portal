package com.jobhub.dao;

import com.jobhub.model.User;
import java.util.Optional;

public interface UserDao extends Dao<User, Long> {
    Optional<User> findByEmail(String email);
}

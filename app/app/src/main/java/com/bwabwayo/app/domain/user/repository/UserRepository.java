package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    // SELECT * FROM user WHERE id = ?1
    User findUserById(String id);
}

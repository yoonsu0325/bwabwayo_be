package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // SELECT * FROM user WHERE id = ?1
    User findById(String id);
}

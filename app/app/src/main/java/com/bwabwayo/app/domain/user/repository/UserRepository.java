package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // SELECT * FROM user WHERE id = ?1
    Optional<User> findById(String id);
}

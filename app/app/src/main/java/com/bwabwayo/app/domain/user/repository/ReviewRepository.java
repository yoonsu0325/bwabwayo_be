package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, String>  {

}

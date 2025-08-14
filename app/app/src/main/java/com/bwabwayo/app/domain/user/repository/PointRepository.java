package com.bwabwayo.app.domain.user.repository;

import com.bwabwayo.app.domain.user.domain.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {

}

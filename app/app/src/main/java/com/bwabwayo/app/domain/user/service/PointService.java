package com.bwabwayo.app.domain.user.service;

import com.bwabwayo.app.domain.user.domain.Point;
import com.bwabwayo.app.domain.user.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {
    private final PointRepository pointRepository;

    public void savePoint(Point point){
        pointRepository.save(point);
    }
}

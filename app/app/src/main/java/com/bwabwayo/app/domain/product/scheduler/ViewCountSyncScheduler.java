package com.bwabwayo.app.domain.product.scheduler;

import com.bwabwayo.app.domain.product.service.ViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final ViewCountService viewCountService;

    @Scheduled(cron = "0 0 * * * *") // 매 정시마다
    public void syncViewCountsToDatabase() {
        viewCountService.syncAllToDatabase();
    }
}

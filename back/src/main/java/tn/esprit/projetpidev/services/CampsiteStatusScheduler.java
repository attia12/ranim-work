// Module: Campsite Status | Layer: Scheduler
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.repositories.CampsiteRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampsiteStatusScheduler {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteStatusEvaluator evaluator;
    private final CampsiteStatusUpdater updater;

    /**
     * Runs every hour. Evaluates all non-deleted, non-manually-suspended campsites.
     * SUSPENDED campsites that were suspended by weather are re-evaluated
     * (they can recover); campsites manually SUSPENDED by admin are skipped
     * since there's no way to distinguish — admin can use /activate to restore.
     */
    @Scheduled(fixedRateString = "${campway.status.refresh-rate-ms:3600000}")
    public void refreshStatuses() {
        List<Campsite> campsites = campsiteRepository.findAllByStatusNot(CampsiteStatus.DELETED);
        int changed = 0;
        for (Campsite campsite : campsites) {
            // Skip manually suspended (admin action) — only re-evaluate auto-manageable states
            if (campsite.getStatus() == CampsiteStatus.SUSPENDED
                    && !isWeatherSuspended(campsite)) {
                continue;
            }
            CampsiteStatusEvaluator.Evaluation eval = evaluator.evaluate(campsite);
            if (updater.applyIfChanged(campsite, eval.status(), eval.reason(), "SCHEDULER")) {
                changed++;
            }
        }
        log.info("Status refresh complete: {} campsite(s) updated out of {}", changed, campsites.size());
    }

    /** Heuristic: a campsite is weather-suspended if the lastStatusReason mentions weather. */
    private boolean isWeatherSuspended(Campsite c) {
        return c.getLastStatusReason() != null && c.getLastStatusReason().startsWith("Severe weather");
    }
}

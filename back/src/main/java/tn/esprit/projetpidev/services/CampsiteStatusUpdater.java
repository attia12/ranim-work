// Module: Campsite Status | Layer: Service
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Campsite;
import tn.esprit.projetpidev.domain.CampsiteStatusHistory;
import tn.esprit.projetpidev.domain.enums.CampsiteStatus;
import tn.esprit.projetpidev.repositories.CampsiteRepository;
import tn.esprit.projetpidev.repositories.CampsiteStatusHistoryRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampsiteStatusUpdater {

    private final CampsiteRepository campsiteRepository;
    private final CampsiteStatusHistoryRepository historyRepository;

    /**
     * Applies newStatus to the campsite if it differs from the current status.
     * Saves a history entry in both cases (change and no-change are both recorded only on change).
     */
    @Transactional
    public boolean applyIfChanged(Campsite campsite, CampsiteStatus newStatus, String reason, String changedBy) {
        if (campsite.getStatus() == newStatus) return false;

        CampsiteStatus previous = campsite.getStatus();
        campsite.setStatus(newStatus);
        campsite.setLastStatusUpdate(LocalDateTime.now());
        campsite.setLastStatusReason(reason);
        campsiteRepository.save(campsite);

        historyRepository.save(CampsiteStatusHistory.builder()
                .campsite(campsite)
                .previousStatus(previous)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .build());

        log.info("Campsite {} status: {} → {} ({})", campsite.getId(), previous, newStatus, reason);
        return true;
    }
}

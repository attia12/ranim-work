// Module: Outdoor Campsite & Booking | Layer: Service Interface
package tn.esprit.projetpidev.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.projetpidev.dto.outdoorcampsite.ModerationRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteResponse;

public interface IOutdoorCampsiteService {

    OutdoorCampsiteResponse propose(OutdoorCampsiteRequest request, Long proposerId);

    OutdoorCampsiteResponse update(Long id, OutdoorCampsiteRequest request, Long requesterId);

    OutdoorCampsiteResponse moderate(Long id, ModerationRequest request, Long adminId);

    OutdoorCampsiteResponse getById(Long id);

    Page<OutdoorCampsiteResponse> getApproved(Pageable pageable);

    Page<OutdoorCampsiteResponse> getPending(Pageable pageable);

    Page<OutdoorCampsiteResponse> getMyProposals(Long userId, Pageable pageable);
}

// Module: Outdoor Campsite & Booking | Layer: Service Implementation
package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.OutdoorCampsite;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.OutdoorCampsiteStatus;
import tn.esprit.projetpidev.dto.outdoorcampsite.ModerationRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteRequest;
import tn.esprit.projetpidev.dto.outdoorcampsite.OutdoorCampsiteResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.OutdoorCampsiteRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class IOutdoorCampsiteServiceImpl implements IOutdoorCampsiteService {

    private final OutdoorCampsiteRepository outdoorCampsiteRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public OutdoorCampsiteResponse propose(OutdoorCampsiteRequest request, Long proposerId) {
        User proposer = userRepository.findById(proposerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", proposerId));

        OutdoorCampsite site = OutdoorCampsite.builder()
                .name(request.getName())
                .description(request.getDescription())
                .country(request.getCountry())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .pictures(request.getPictures())
                .naturalFeatures(request.getNaturalFeatures())
                .accessDifficulty(request.getAccessDifficulty())
                .proposedBy(proposer)
                .status(OutdoorCampsiteStatus.PENDING)
                .build();

        OutdoorCampsite saved = outdoorCampsiteRepository.save(site);
        log.info("OutdoorCampsite proposed: id={}, proposer={}", saved.getId(), proposerId);
        return mapToResponse(saved);
    }

    @Override
    public OutdoorCampsiteResponse update(Long id, OutdoorCampsiteRequest request, Long requesterId) {
        OutdoorCampsite site = findSite(id);
        if (!site.getProposedBy().getId().equals(requesterId)) {
            throw new IllegalStateException("Only the proposer can edit this outdoor campsite.");
        }
        if (site.getStatus() != OutdoorCampsiteStatus.PENDING) {
            throw new IllegalStateException("Only PENDING proposals can be edited.");
        }

        site.setName(request.getName());
        site.setDescription(request.getDescription());
        site.setCountry(request.getCountry());
        site.setCity(request.getCity());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        site.setPictures(request.getPictures());
        site.setNaturalFeatures(request.getNaturalFeatures());
        site.setAccessDifficulty(request.getAccessDifficulty());
        return mapToResponse(outdoorCampsiteRepository.save(site));
    }

    @Override
    public OutdoorCampsiteResponse moderate(Long id, ModerationRequest request, Long adminId) {
        OutdoorCampsite site = findSite(id);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adminId));

        String action = request.getAction().toUpperCase();
        switch (action) {
            case "APPROVE" -> {
                site.setStatus(OutdoorCampsiteStatus.APPROVED);
                site.setApprovedBy(admin);
                site.setApprovedAt(LocalDateTime.now());
                site.setAdminNote(request.getAdminNote());
                // Notify proposer
                try {
                    emailService.sendOutdoorCampsiteApprovalEmail(
                            site.getProposedBy().getEmail(),
                            site.getProposedBy().getFullname(),
                            site.getName(),
                            true,
                            request.getAdminNote()
                    );
                } catch (Exception e) {
                    log.warn("Failed to send outdoor approval email: {}", e.getMessage());
                }
            }
            case "REJECT" -> {
                site.setStatus(OutdoorCampsiteStatus.REJECTED);
                site.setAdminNote(request.getAdminNote());
                try {
                    emailService.sendOutdoorCampsiteApprovalEmail(
                            site.getProposedBy().getEmail(),
                            site.getProposedBy().getFullname(),
                            site.getName(),
                            false,
                            request.getAdminNote()
                    );
                } catch (Exception e) {
                    log.warn("Failed to send outdoor rejection email: {}", e.getMessage());
                }
            }
            case "SUSPEND" -> site.setStatus(OutdoorCampsiteStatus.SUSPENDED);
            default -> throw new IllegalArgumentException("Unknown moderation action: " + action);
        }

        log.info("OutdoorCampsite moderated: id={}, action={}", id, action);
        return mapToResponse(outdoorCampsiteRepository.save(site));
    }

    @Override
    @Transactional(readOnly = true)
    public OutdoorCampsiteResponse getById(Long id) {
        return mapToResponse(findSite(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutdoorCampsiteResponse> getApproved(Pageable pageable) {
        return outdoorCampsiteRepository.findByStatus(OutdoorCampsiteStatus.APPROVED, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutdoorCampsiteResponse> getPending(Pageable pageable) {
        return outdoorCampsiteRepository.findByStatus(OutdoorCampsiteStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OutdoorCampsiteResponse> getMyProposals(Long userId, Pageable pageable) {
        return outdoorCampsiteRepository.findByProposedBy_Id(userId, pageable)
                .map(this::mapToResponse);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private OutdoorCampsite findSite(Long id) {
        return outdoorCampsiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OutdoorCampsite", id));
    }

    OutdoorCampsiteResponse mapToResponse(OutdoorCampsite s) {
        OutdoorCampsiteResponse r = new OutdoorCampsiteResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setDescription(s.getDescription());
        r.setCountry(s.getCountry());
        r.setCity(s.getCity());
        r.setLatitude(s.getLatitude());
        r.setLongitude(s.getLongitude());
        r.setPictures(splitCsv(s.getPictures()));
        r.setNaturalFeatures(splitCsv(s.getNaturalFeatures()));
        r.setAccessDifficulty(s.getAccessDifficulty());
        r.setProposedById(s.getProposedBy().getId());
        r.setProposedByName(s.getProposedBy().getFullname());
        r.setStatus(s.getStatus());
        r.setAdminNote(s.getAdminNote());
        r.setApprovedAt(s.getApprovedAt());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        if (s.getApprovedBy() != null) {
            r.setApprovedById(s.getApprovedBy().getId());
            r.setApprovedByName(s.getApprovedBy().getFullname());
        }
        return r;
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}

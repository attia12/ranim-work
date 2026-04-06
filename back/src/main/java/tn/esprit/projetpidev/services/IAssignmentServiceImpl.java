package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.Assignment;
import tn.esprit.projetpidev.domain.Event;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.userprofile.GuideProfile;
import tn.esprit.projetpidev.dto.assignment.AssignmentRequest;
import tn.esprit.projetpidev.dto.assignment.AssignmentResponse;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.repositories.AssignmentRepository;
import tn.esprit.projetpidev.repositories.EventRepository;
import tn.esprit.projetpidev.repositories.GuideProfileRepository;
import tn.esprit.projetpidev.repositories.UserRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class IAssignmentServiceImpl implements IAssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final GuideProfileRepository guideProfileRepository; // only for verified check

    @Override
    public AssignmentResponse createAssignment(AssignmentRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        // Resolve guide as a plain User — same as Delivery resolves camper
        User guide = userRepository.findById(request.getGuideUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getGuideUserId()));

        // Guard: must be a GUIDE role user
        if (guide.getRole() != Role.GUIDE) {
            throw new IllegalArgumentException(
                    "User " + guide.getId() + " does not have the GUIDE role.");
        }

        // Guard: only verified guides can be assigned (mirrors claimDelivery verified-agent check)
        GuideProfile guideProfile = guideProfileRepository.findByUserId(guide.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User " + guide.getId() + " has no GuideProfile. Cannot assign."));
        if (!Boolean.TRUE.equals(guideProfile.getIsVerified())) {
            throw new IllegalArgumentException(
                    "Guide " + guide.getId() + " is not yet verified.");
        }

        // Guard: no duplicate assignment
        if (assignmentRepository.existsByEvent_EventIdAndGuide_Id(
                event.getEventId(), guide.getId())) {
            throw new RuntimeException(
                    "Guide " + guide.getId() + " is already assigned to event " + event.getEventId());
        }

        Assignment assignment = Assignment.builder()
                .event(event)
                .guide(guide)
                .roleDescription(request.getRoleDescription())
                .status(request.getStatus() != null ? request.getStatus() : "ASSIGNED")
                .build();

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        return mapToResponse(assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAssignments() {
        return assignmentRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByEvent(Long eventId) {
        return assignmentRepository.findByEvent_EventId(eventId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByGuide(Long guideUserId) {
        return assignmentRepository.findByGuide_Id(guideUserId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", request.getEventId()));

        User guide = userRepository.findById(request.getGuideUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getGuideUserId()));

        if (guide.getRole() != Role.GUIDE) {
            throw new IllegalArgumentException(
                    "User " + guide.getId() + " does not have the GUIDE role.");
        }

        assignment.setEvent(event);
        assignment.setGuide(guide);
        assignment.setRoleDescription(request.getRoleDescription());
        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    public AssignmentResponse updateStatus(Long assignmentId, String status) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
        assignment.setStatus(status);
        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Override
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new ResourceNotFoundException("Assignment", assignmentId);
        }
        assignmentRepository.deleteById(assignmentId);
    }

    // ── mapper ───────────────────────────────────────────────────────────────
    // Mirrors DeliveryResponse.camperFullName — User fields accessed directly

    private AssignmentResponse mapToResponse(Assignment a) {
        AssignmentResponse r = new AssignmentResponse();
        r.setAssignmentId(a.getAssignmentId());

        r.setEventId(a.getEvent().getEventId());
        r.setEventTitle(a.getEvent().getTitle());
        r.setEventDate(a.getEvent().getDate());
        r.setEventLocation(a.getEvent().getLocation());

        r.setGuideUserId(a.getGuide().getId());
        r.setGuideFullName(a.getGuide().getFullname());
        r.setGuideEmail(a.getGuide().getEmail());

        r.setRoleDescription(a.getRoleDescription());
        r.setStatus(a.getStatus());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());
        return r;
    }
}

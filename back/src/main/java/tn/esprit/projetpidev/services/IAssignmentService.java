package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.assignment.AssignmentRequest;
import tn.esprit.projetpidev.dto.assignment.AssignmentResponse;

import java.util.List;

public interface IAssignmentService {

    AssignmentResponse createAssignment(AssignmentRequest request);

    AssignmentResponse getAssignmentById(Long assignmentId);

    List<AssignmentResponse> getAllAssignments();

    List<AssignmentResponse> getAssignmentsByEvent(Long eventId);

    List<AssignmentResponse> getAssignmentsByGuide(Long guideUserId);

    AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request);

    AssignmentResponse updateStatus(Long assignmentId, String status);

    void deleteAssignment(Long assignmentId);
}

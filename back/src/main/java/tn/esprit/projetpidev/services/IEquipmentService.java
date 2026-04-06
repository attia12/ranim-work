package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.dto.equipment.EquipmentRequest;
import tn.esprit.projetpidev.dto.equipment.EquipmentResponse;

import java.util.List;
import java.util.Map;

public interface IEquipmentService {
    EquipmentResponse createEquipment(EquipmentRequest request, User loggedInUser);
    EquipmentResponse getEquipmentById(Long id);
    List<EquipmentResponse> getAllEquipment();
    List<EquipmentResponse> getEquipmentByCategory(Long categoryId);
    List<EquipmentResponse> getEquipmentByOwner(Long ownerId);
    List<EquipmentResponse> searchEquipment(String keyword);
    EquipmentResponse updateEquipment(Long id, EquipmentRequest request, User loggedInUser);
    void deleteEquipment(Long id, User loggedInUser);

    // ── Availability ──────────────────────────────────────────────────────────
    List<Map<String, String>> getUnavailablePeriods(Long equipmentId);
    List<Map<String, String>> getBlockedPeriods(Long equipmentId);
}
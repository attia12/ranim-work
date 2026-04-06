package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.Role;
import tn.esprit.projetpidev.domain.enums.TransactionType;
import tn.esprit.projetpidev.domain.User;
import tn.esprit.projetpidev.exception.ResourceNotFoundException;
import tn.esprit.projetpidev.dto.equipment.EquipmentRequest;
import tn.esprit.projetpidev.dto.equipment.EquipmentResponse;
import tn.esprit.projetpidev.domain.Equipment;
import tn.esprit.projetpidev.domain.EquipmentCategory;
import tn.esprit.projetpidev.domain.EquipmentPhoto;
import tn.esprit.projetpidev.domain.OrderItem;
import tn.esprit.projetpidev.repositories.EquipmentCategoryRepository;
import tn.esprit.projetpidev.repositories.EquipmentRepository;
import tn.esprit.projetpidev.repositories.OrderItemRepository;
import tn.esprit.projetpidev.repositories.WarehouseRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

@Service
@Transactional
@RequiredArgsConstructor
public class IEquipmentServiceImpl implements IEquipmentService {

    private final EquipmentRepository       equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    private final WarehouseRepository        warehouseRepository;
    private final OrderItemRepository        orderItemRepository;   // ← added

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public EquipmentResponse createEquipment(EquipmentRequest request, User loggedInUser) {
        if (loggedInUser.getRole() != Role.EQUIPEMENTPROVIEDERS && loggedInUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only equipment providers can list equipment");
        }

        EquipmentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .purchasePrice(request.getPurchasePrice())
                .availableForRent(request.isAvailableForRent())
                .availableForSale(request.isAvailableForSale())
                .stock(request.getStock())
                .photos(new ArrayList<>())
                .specifications(request.getSpecifications())
                .condition(request.getCondition())
                .weight(request.getWeight())
                .category(category)
                .owner(loggedInUser)
                .warehouse(
                        request.getWarehouseId() != null
                                ? warehouseRepository.findById(request.getWarehouseId()).orElse(null)
                                : null
                )
                .build();

        Equipment saved = equipmentRepository.save(equipment);

        if (request.getPhotos() != null) {
            List<EquipmentPhoto> photoEntities = request.getPhotos().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> EquipmentPhoto.builder().url(url).equipment(saved).build())
                    .collect(toCollection(ArrayList::new));
            saved.setPhotos(photoEntities);
            equipmentRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        return mapToResponse(equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentByCategory(Long categoryId) {
        return equipmentRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> getEquipmentByOwner(Long ownerId) {
        return equipmentRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> searchEquipment(String keyword) {
        return equipmentRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request, User loggedInUser) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        boolean isOwner = equipment.getOwner().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can update this equipment");
        }

        EquipmentCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setPricePerDay(request.getPricePerDay());
        equipment.setPurchasePrice(request.getPurchasePrice());
        equipment.setAvailableForRent(request.isAvailableForRent());
        equipment.setAvailableForSale(request.isAvailableForSale());
        equipment.setStock(request.getStock());
        equipment.setSpecifications(request.getSpecifications());
        equipment.setCondition(request.getCondition());
        equipment.setWeight(request.getWeight());
        equipment.setCategory(category);

        if (request.getWarehouseId() != null) {
            warehouseRepository.findById(request.getWarehouseId())
                    .ifPresent(equipment::setWarehouse);
        } else {
            equipment.setWarehouse(null);
        }

        equipment.getPhotos().clear();
        if (request.getPhotos() != null) {
            List<EquipmentPhoto> newPhotos = request.getPhotos().stream()
                    .filter(url -> url != null && !url.isBlank())
                    .map(url -> EquipmentPhoto.builder().url(url).equipment(equipment).build())
                    .collect(Collectors.toCollection(ArrayList::new));
            equipment.getPhotos().addAll(newPhotos);
        }

        return mapToResponse(equipmentRepository.save(equipment));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void deleteEquipment(Long id, User loggedInUser) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        boolean isOwner = equipment.getOwner().getId().equals(loggedInUser.getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the owner or an admin can delete this equipment");
        }
        equipmentRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ── NEW: Unavailable periods (active rental orders for this equipment) ───
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns periods when the equipment is unavailable for new rentals.
     * These are derived from RENTAL OrderItems whose parent Order is not CANCELLED.
     * Because OrderItem currently only stores rentalDays (no start/end dates),
     * we compute a synthetic period: createdAt → createdAt + rentalDays.
     * Once you add rentalStart/rentalEnd columns to OrderItem, replace the logic below.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, String>> getUnavailablePeriods(Long equipmentId) {
        return orderItemRepository.findByEquipmentId(equipmentId).stream()
                .filter(oi -> oi.getTransactionType() == TransactionType.RENT)
                .filter(oi -> oi.getOrder() != null
                        && oi.getOrder().getStatus() != OrderStatus.CANCELLED)
                .filter(oi -> oi.getRentalDays() != null && oi.getRentalDays() > 0)
                .map(oi -> {
                    LocalDate start = oi.getCreatedAt() != null
                            ? oi.getCreatedAt().toLocalDate()
                            : LocalDate.now();
                    LocalDate end = start.plusDays(oi.getRentalDays());
                    Map<String, String> period = new LinkedHashMap<>();
                    period.put("startDate", start.toString());
                    period.put("endDate",   end.toString());
                    period.put("type",      "RENTED");
                    period.put("reason",    "Reserved by customer");
                    return period;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns manually blocked periods set by the equipment owner.
     * Currently there is no BlockedPeriod entity — returns empty list.
     * Add a BlockedPeriod entity + repository and replace this stub.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, String>> getBlockedPeriods(Long equipmentId) {
        // TODO: implement once a BlockedPeriod entity is added
        return new ArrayList<>();
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private EquipmentResponse mapToResponse(Equipment e) {
        EquipmentResponse r = new EquipmentResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setPricePerDay(e.getPricePerDay());
        r.setPurchasePrice(e.getPurchasePrice());
        r.setAvailableForRent(e.getAvailableForRent());
        r.setAvailableForSale(e.getAvailableForSale());
        r.setStock(e.getStock());

        List<String> photoUrls = e.getPhotos() != null
                ? e.getPhotos().stream().map(EquipmentPhoto::getUrl).toList()
                : new ArrayList<>();
        r.setPhotos(photoUrls);

        r.setSpecifications(e.getSpecifications());
        r.setCondition(e.getCondition());
        r.setWeight(e.getWeight());
        r.setCategoryId(e.getCategory().getId());
        r.setCategoryName(e.getCategory().getName());
        r.setOwnerId(e.getOwner().getId());
        r.setOwnerFullName(e.getOwner().getFullname());
        r.setCreatedAt(e.getCreatedAt());

        if (e.getWarehouse() != null) {
            r.setWarehouseId(e.getWarehouse().getId());
            r.setWarehouseName(e.getWarehouse().getName());
        }
        return r;
    }
}
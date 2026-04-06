package tn.esprit.projetpidev.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.projetpidev.domain.*;
import tn.esprit.projetpidev.dto.groupe.GroupeRequest;
import tn.esprit.projetpidev.dto.groupe.GroupeResponse;
import tn.esprit.projetpidev.repositories.*;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IGroupeServiceImpl implements IGroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepo;

    // Create a new group
    @Override
    public GroupeResponse createGroupe(GroupeRequest request) {
        if (groupeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Group name already exists");
        }

        Groupe groupe = Groupe.builder()
                .name(request.getName())
                .isPrivate(request.isPrivate())
                .createAt(new Date())
                .build();

        return mapToResponse(groupeRepository.save(groupe));
    }

    // Get group by ID
    @Override
    public GroupeResponse getGroupeById(Long groupeId) {
        return mapToResponse(groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Group not found")));
    }

    // Get all groups
    @Override
    public List<GroupeResponse> getAllGroupes() {
        return groupeRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    // Get public groups only
    @Override
    public List<GroupeResponse> getPublicGroupes() {
        return groupeRepository.findByIsPrivate(false)
                .stream().map(this::mapToResponse).toList();
    }

    // Update a group
    @Override
    public GroupeResponse updateGroupe(Long groupeId, GroupeRequest request) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        groupe.setName(request.getName());
        groupe.setPrivate(request.isPrivate());
        return mapToResponse(groupeRepository.save(groupe));
    }

    // Delete a group
    @Override
    public void deleteGroupe(Long groupeId) {
        groupeRepository.deleteById(groupeId);
    }


    // Map Groupe to GroupeResponse
    private GroupeResponse mapToResponse(Groupe groupe) {
        GroupeResponse response = new GroupeResponse();
        response.setGroupeId(groupe.getGroupeId());
        response.setName(groupe.getName());
        response.setPrivate(groupe.isPrivate());
        response.setCreateAt(groupe.getCreateAt());
        response.setTotalMembers(
                groupe.getUsers() != null ? groupe.getUsers().size() : 0
        );
        return response;
    }
    @Override
    public Groupe addUserToGroupe(Long groupeId, Long userId) {
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
        User user = userRepo.findById(userId).orElse(null);
        if (groupe == null || user == null) return null;
        if (!groupe.getUsers().contains(user)) {
            groupe.getUsers().add(user);
        }
        return groupeRepository.save(groupe);
    }

    @Override
    public Groupe removeUserFromGroupe(Long groupeId, Long userId) {
        Groupe groupe = groupeRepository.findById(groupeId).orElse(null);
        User user = userRepo.findById(userId).orElse(null);
        if (groupe == null || user == null) return null;
        groupe.getUsers().remove(user);
        return groupeRepository.save(groupe);
    }

}
package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.domain.Groupe;
import tn.esprit.projetpidev.dto.groupe.GroupeRequest;
import tn.esprit.projetpidev.dto.groupe.GroupeResponse;
import java.util.List;

public interface IGroupeService {
    GroupeResponse createGroupe(GroupeRequest request);
    GroupeResponse getGroupeById(Long groupeId);
    List<GroupeResponse> getAllGroupes();
    List<GroupeResponse> getPublicGroupes();
    GroupeResponse updateGroupe(Long groupeId, GroupeRequest request);
    void deleteGroupe(Long groupeId);
    Groupe addUserToGroupe(Long groupeId, Long userId);
    Groupe removeUserFromGroupe(Long groupeId, Long userId);
}
package tn.esprit.projetpidev.services;

import tn.esprit.projetpidev.dto.like.LikeRequest;
import tn.esprit.projetpidev.dto.like.LikeResponse;

import java.util.List;

public interface ILikeService {
    LikeResponse likePost(LikeRequest request);
    void unlikePost(Long userId, Long postId);
    List<LikeResponse> getLikesByPost(Long postId);
    List<LikeResponse> getLikesByUser(Long userId);

}
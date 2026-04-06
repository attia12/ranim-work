package tn.esprit.projetpidev.dto.like;

import lombok.Data;

@Data
public class LikeResponse {
    private Long likeId;
    private String username;
    private Long postId;
    private String postTitle;
}
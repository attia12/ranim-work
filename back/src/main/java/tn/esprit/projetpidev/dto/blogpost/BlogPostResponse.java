package tn.esprit.projetpidev.dto.blogpost;

import lombok.Data;

@Data
public class BlogPostResponse {
    private Long postId;
    private String title;
    private String content;
    private String categoryName;
    private String authorName;
    private int totalComments;
    private int totalLikes;
}
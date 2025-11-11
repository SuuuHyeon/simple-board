package com.example.board.dto;

import com.example.board.domain.Post;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String authorNickname;


    // 엔티티를 받아서 변환해주는 메서드
    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getMember().getNickname())
                .build();
    }
}

package com.example.board.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시물 수정 DTO
 * PostCreateRequestDto를 공용으로 사용하다 validation을 적용 중 따로 분리하는 게 나을 것 같다고 생각돼서 분리
 */
@Getter @Setter
public class PostUpdateRequest {

    @Size(max = 100, message = "제목 100자를 넘길 수 없습니다.")
    private String title;

    private String content;
}

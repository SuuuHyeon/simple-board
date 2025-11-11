package com.example.board.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class Post {

    @Id     // PK
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 게시물 업데이트 메서드
    public void updatePost(String newTitle, String newContent) {

        // null 값 처리
        if (newTitle != null && !newTitle.isBlank()) {
            this.title = newTitle;
        }
        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent;
        }


    }

    public void setMember(Member member) {
        if (this.member != null) {
            throw new IllegalArgumentException("작성자를 수정할 수 없습니다.");
        }
        this.member = member;
    }
}

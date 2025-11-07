package com.example.board.service;

import com.example.board.domain.Post;
import com.example.board.dto.PostCreateRequest;
import com.example.board.dto.PostResponse;
import com.example.board.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    /**
     * 게시물 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        // post 생성
        Post post = new Post();

        // 값 세팅
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // db 저장
        Post savedPost = postRepository.save(post);

        // Postresponse 형태로 반환
        return new PostResponse(savedPost);
    }

    /**
     * 게시물 조회
     */
    public PostResponse getPostById(Long id) {
        // 엔티티 조회
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시물입니다.") // 에러 처리
        );

        return new PostResponse(post);
    }
}

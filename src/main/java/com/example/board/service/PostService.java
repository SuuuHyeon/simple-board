package com.example.board.service;

import com.example.board.domain.Post;
import com.example.board.dto.PostCreateRequest;
import com.example.board.dto.PostResponse;
import com.example.board.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
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
     * 게시물 조회 (단일)
     */
    public PostResponse getPostById(Long id) {
        // 엔티티 조회
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시물입니다.") // 에러 처리
        );

        return new PostResponse(post);
    }

    /**
     * 게시물 조회 (전체)
     */
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts.stream().map(PostResponse::new).toList();
    }

    /**
     * 게시물 수정
     */
    @Transactional
    public PostResponse updatePost(Long id, PostCreateRequest request) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );

        //  JPA의 변경 감지 기능으로 자동으로 저장됨 (save 안 해도 됨)
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        return new PostResponse(post);
    }

    /**
     * 게시물 삭제
     */
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );

        postRepository.delete(post);
    }
}

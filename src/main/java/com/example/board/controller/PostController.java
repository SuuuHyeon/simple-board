package com.example.board.controller;


import com.example.board.dto.PostCreateRequest;
import com.example.board.dto.PostResponse;
import com.example.board.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 게시물 생성
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateRequest request) {

        PostResponse response = postService.createPost(request);

        return ResponseEntity.created(URI.create("/posts" + response.getId())).body(response);
    }

    /**
     * 게시물 조회 (단일)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);

        return ResponseEntity.ok(post);
    }

    /**
     * 게시물 조회 (리스트)
     */
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> allPosts = postService.getAllPosts();

        return ResponseEntity.ok(allPosts);
    }

    /**
     * 게시물 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id, @RequestBody PostCreateRequest request) {
        PostResponse postResponse = postService.updatePost(id, request);

        return ResponseEntity.ok(postResponse);
    }

    /**
     * 게시물 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);

        return ResponseEntity.noContent().build();
    }
}

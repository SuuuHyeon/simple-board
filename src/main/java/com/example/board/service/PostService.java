package com.example.board.service;

import com.example.board.domain.Member;
import com.example.board.domain.Post;
import com.example.board.dto.PostCreateRequest;
import com.example.board.dto.PostResponse;
import com.example.board.dto.PostUpdateRequest;
import com.example.board.repository.MemberRepository;
import com.example.board.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;

    /**
     * 게시물 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Long memberId) {

        // 멤버 먼저 찾기
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("회원정보를 찾을 수 없습니다.")
        );

        // modelMapper 적용
        Post post = modelMapper.map(request, Post.class);
        // member 필드가 없으므로 따로 주입
        post.setMember(member);

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
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        log.info("============= 게시물 수정 서비스 진입 =============");
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );

        //  JPA의 변경 감지 기능으로 자동으로 저장됨 (save 안 해도 됨)

//        post.setTitle(request.getTitle());
//        post.setContent(request.getContent());

        // -> null값 처리
        if (request.getTitle() != null && request.getTitle().isBlank())
            post.setTitle(request.getTitle());
        if (request.getContent() != null && request.getContent().isBlank())
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

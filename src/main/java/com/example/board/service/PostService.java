package com.example.board.service;

import com.example.board.domain.Member;
import com.example.board.domain.Post;
import com.example.board.dto.PostCreateRequest;
import com.example.board.dto.PostResponse;
import com.example.board.dto.PostUpdateRequest;
import com.example.board.repository.MemberRepository;
import com.example.board.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 게시물 생성
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request, Principal principal) {

        String email = principal.getName();     // principl에서 email 값 추출

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")
        );

        Post post = Post.create(request.getTitle(), request.getContent(), member);

        // db 저장
        Post savedPost = postRepository.save(post);

        // Postresponse 형태로 반환
        return PostResponse.fromEntity(savedPost);
    }

    /**
     * 게시물 조회 (단일)
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        // 엔티티 조회
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시물입니다.") // 에러 처리
        );

        return PostResponse.fromEntity(post);
    }

    /**
     * 게시물 조회 (전체)
     */
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAll();

        return posts.stream().map(PostResponse::fromEntity).toList();
    }

    /**
     * 게시물 수정
     */
    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request, Principal principal) {


        log.info("============= 게시물 수정 서비스 진입 =============");
        Post post = postRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );

        if (!post.getMember().getEmail().equals(principal.getName())) {
            throw new IllegalArgumentException("게시물 수정 권한이 없습니다.");
        }
        //  JPA의 변경 감지 기능으로 자동으로 저장됨 (save 안 해도 됨)

        // 수정 메서드 호출
        post.update(request.getTitle(), request.getContent());

        // JPA 변경 감지로 save 할 필요 없음

        return PostResponse.fromEntity(post);
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

package com.example.board.service;

import com.example.board.domain.Member;
import com.example.board.dto.MemberSignupRequest;
import com.example.board.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Service
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberService {

    private final MemberRepository memberRepository;        // MemberRepository 주입
    private final PasswordEncoder passwordEncoder;          // PasswordEncoder 주입


    @Transactional
    @PostMapping("/signup")
    public Long signup(@RequestBody MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setPassword(passwordEncoder.encode(request.getPassword())); // 패스워드 암호화 추가
        member.setNickname(request.getNickname());

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }


}

package com.example.board.service;

import com.example.board.config.jwt.JwtTokenProvider;
import com.example.board.domain.Member;
import com.example.board.dto.MemberLoginRequest;
import com.example.board.dto.MemberSignupRequest;
import com.example.board.dto.TokenResponse;
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
    private final JwtTokenProvider jwtTokenProvider;


    // 회원가입
    @Transactional
    public Long signup(MemberSignupRequest request) {
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


    // 로그인
    public TokenResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("가입되지 않은 이메일입니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String token = jwtTokenProvider.createToken(member.getEmail(), member.getId());

        return new TokenResponse(token);
    }


}

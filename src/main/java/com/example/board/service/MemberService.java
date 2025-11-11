package com.example.board.service;

import com.example.board.config.jwt.JwtTokenProvider;
import com.example.board.domain.Member;
import com.example.board.domain.RefreshToken;
import com.example.board.domain.TokenReissueRequest;
import com.example.board.dto.MemberLoginRequest;
import com.example.board.dto.MemberSignupRequest;
import com.example.board.dto.TokenResponse;
import com.example.board.repository.MemberRepository;
import com.example.board.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;


    // 회원가입
    @Transactional
    public Long signup(MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 엔티티 noargsconstructor 어노테이션 추가로 인한 수정사항
        Member member = new Member(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }


    @Transactional
    // 로그인
    public TokenResponse login(MemberLoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("가입되지 않은 이메일입니다.")
        );

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        // 이미 토큰이 존재하면 값 교체
                        (token) -> token.updateTokenValue(refreshToken),
                        // 없다면 새로 생성해서 저장
                        () -> refreshTokenRepository.save(new RefreshToken(member, refreshToken))
                );


        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Transactional
    // 토큰 재발급 메서드
    public TokenResponse reissue(TokenReissueRequest request) {

        String refreshTokenValue = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenValue(request.getRefreshToken()).orElseThrow(
                () -> new IllegalArgumentException("Refresh Token이 존재하지 않습니다.")
        );

        // 유저 정보
        Member member = refreshToken.getMember();

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getId());
        // 실무에서는 보안을 위해 새 것으로 교체한다고 함
        // Refresh Token Rotation(RTR) 전략
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        refreshToken.updateTokenValue(newRefreshToken);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}

package com.example.board.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String tokenValue;


    public RefreshToken(Member member, String tokenValue) {
        this.member = member;
        this.tokenValue = tokenValue;
    }

    public void updateTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

}

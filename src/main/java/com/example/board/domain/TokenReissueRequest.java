package com.example.board.domain;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenReissueRequest {

    @NotEmpty(message = "Refresh Token이 필요합니다.")
    private String refreshToken;
}

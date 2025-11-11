package com.example.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;

}

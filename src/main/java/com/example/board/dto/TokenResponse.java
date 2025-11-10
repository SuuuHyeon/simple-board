package com.example.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TokenResponse {

    private String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}

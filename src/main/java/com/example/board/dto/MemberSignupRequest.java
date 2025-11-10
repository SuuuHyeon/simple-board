package com.example.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberSignupRequest {

    @NotEmpty(message = "이메일은 필수입니다.")
    @Email      ///  TODO: 추후 조건 고도화(제대로 못 잡음)
    private String email;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotEmpty(message = "닉네임은 필수입니다.")
    private String nickname;
}

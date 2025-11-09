package com.example.board.controller;


import com.example.board.dto.MemberResponse;
import com.example.board.dto.MemberSignupRequest;
import com.example.board.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;


    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(/*@Valid*/ @RequestBody MemberSignupRequest request) {

        Long memberId = memberService.signup(request);

        return ResponseEntity.created(URI.create("/members/" + memberId)).build();
    }


}

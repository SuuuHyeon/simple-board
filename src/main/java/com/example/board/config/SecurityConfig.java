package com.example.board.config;


import com.example.board.config.jwt.JwtAuthenticationFilter;
import com.example.board.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 스프링 시큐리티 설정 파일
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    ///  패스워드 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private final JwtTokenProvider jwtTokenProvider;


    ///  임시로 모두 연결
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->

                                /// 임시로 모두 허용
//                              해당 주소는 열어주기
                                auth.requestMatchers(
                                                "/",                // 루트 (http://localhost:8080/)
                                                "/index.html",    // 메인 HTML
                                                "/app.js",        // 메인 JS
                                                "/style.css",     // 메인 CSS
                                                "/members/login",
                                                "/members/signup",
                                                "/members/reissue",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**"
                                        ).permitAll()
                                        // 나머지 주소는 인증된 멤버만 출입 가능
                                        .anyRequest().authenticated()


                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class); /// TODO: 다시보기

        return http.build();
    }

}

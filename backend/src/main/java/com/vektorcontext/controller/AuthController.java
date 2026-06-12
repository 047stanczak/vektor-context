package com.vektorcontext.controller;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.LoginRequest;
import com.vektorcontext.dto.RegisterRequest;
import com.vektorcontext.models.User;
import com.vektorcontext.security.TokenSecurity;
import com.vektorcontext.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final TokenSecurity tokenSecurity;

    public AuthController(AuthService authService, TokenSecurity tokenSecurity) {
        this.authService = authService;
        this.tokenSecurity = tokenSecurity;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        User user = authService.register(request);

        if (user == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Usuário já existe"));
        }

        return ResponseEntity.ok(ApiResponse.ok("Usuário criado com sucesso"));
    }


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody @Valid LoginRequest request,HttpServletResponse response) {
        User user = authService.authenticate(request);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Usuário ou senha inválidos"));
        }

        String token = tokenSecurity.generateToken(user);

        ResponseCookie cookie = ResponseCookie
                .from("token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso"));
    }

    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.ok("Logout realizado"));
    }
}
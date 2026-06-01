package com.vektorcontext.security;

import java.io.IOException;

import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vektorcontext.models.User;
import com.vektorcontext.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenSecurity tokenSecurity;
    private final UserRepository userRepository;

    public SecurityFilter(TokenSecurity tokenSecurity, UserRepository userRepository) {
        this.tokenSecurity = tokenSecurity;
        this.userRepository = userRepository;
    }

@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

    String token = recoverToken(request);

    if (token != null && !token.isBlank()) {
        String code = tokenSecurity.validateToken(token);

        if (code != null && !code.isBlank()) {
            User user = userRepository.findByCodeUser(code);

            if (user != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        user,  
                        null,  
                        java.util.Collections.emptyList() 
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
    }

    filterChain.doFilter(request, response);
}


    private String recoverToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

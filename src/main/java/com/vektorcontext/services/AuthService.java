package com.vektorcontext.services;

import com.vektorcontext.dto.LoginRequest;
import com.vektorcontext.dto.RegisterRequest;
import com.vektorcontext.models.User;
import com.vektorcontext.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByCodeUser(request.getCodeUser())) {
            return null;
        }

        User user = new User();
        user.setCodeUser(request.getCodeUser());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        return userRepository.save(user);
    }

    public User authenticate(LoginRequest request) {

        User user = userRepository.findByCodeUser(request.getCodeUser());

        if (user == null) {
            return null;
        }

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            return null;
        }

        return user;
    }
}
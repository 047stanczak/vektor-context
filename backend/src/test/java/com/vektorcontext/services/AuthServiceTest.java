package com.vektorcontext.services;

import com.vektorcontext.dto.LoginRequest;
import com.vektorcontext.dto.RegisterRequest;
import com.vektorcontext.models.User;
import com.vektorcontext.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        when(userRepository.existsByCodeUser("user01")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        RegisterRequest request = new RegisterRequest();
        request.setCodeUser("user01");
        request.setPassword("pass");

        User result = authService.register(request);

        assertNotNull(result);
        assertEquals("user01", result.getCodeUser());
        assertEquals("hashed", result.getPassword());
    }

    @Test
    void register_duplicateUser_returnsNull() {
        when(userRepository.existsByCodeUser("user01")).thenReturn(true);

        RegisterRequest request = new RegisterRequest();
        request.setCodeUser("user01");
        request.setPassword("pass");

        User result = authService.register(request);

        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_success() {
        User user = new User();
        user.setCodeUser("user01");
        user.setPassword("hashed");

        when(userRepository.findByCodeUser("user01")).thenReturn(user);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.setCodeUser("user01");
        request.setPassword("pass");

        User result = authService.authenticate(request);

        assertNotNull(result);
        assertEquals("user01", result.getCodeUser());
    }

    @Test
    void authenticate_userNotFound_returnsNull() {
        when(userRepository.findByCodeUser(anyString())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.setCodeUser("unknown");
        request.setPassword("pass");

        User result = authService.authenticate(request);

        assertNull(result);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_wrongPassword_returnsNull() {
        User user = new User();
        user.setCodeUser("user01");
        user.setPassword("hashed");

        when(userRepository.findByCodeUser("user01")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setCodeUser("user01");
        request.setPassword("wrong");

        User result = authService.authenticate(request);

        assertNull(result);
    }
}
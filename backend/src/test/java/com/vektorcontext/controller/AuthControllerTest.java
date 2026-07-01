package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.LoginRequest;
import com.vektorcontext.dto.RegisterRequest;
import com.vektorcontext.exception.InvalidCredentialsException;
import com.vektorcontext.exception.UserAlreadyExistsException;
import com.vektorcontext.models.User;
import com.vektorcontext.security.TokenSecurity;
import com.vektorcontext.services.AuthService;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private TokenSecurity tokenSecurity;

    @InjectMocks
    private AuthController controller;

    @Test
    void register_sucesso() {
        RegisterRequest request = new RegisterRequest();
        request.setCodeUser("user1");
        request.setPassword("pass123");

        User user = new User();
        user.setId(1L);
        user.setCodeUser("user1");
        user.setPassword("encodedPass");

        when(authService.register(request)).thenReturn(user);

        ResponseEntity<ApiResponse<Void>> response = controller.register(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.status()).isEqualTo(200);
        assertThat(body.message()).isEqualTo("Usuário criado com sucesso");
        verify(authService).register(request);
    }

    @Test
    void register_usuarioJaExiste_lancaExcecao() {
        RegisterRequest request = new RegisterRequest();
        request.setCodeUser("user1");
        request.setPassword("pass123");

        when(authService.register(request)).thenReturn(null);

        assertThatThrownBy(() -> controller.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Usuário já existe");
    }

    @Test
    void login_sucesso_e_armazena_token_no_cookie() {
        LoginRequest request = new LoginRequest();
        request.setCodeUser("user1");
        request.setPassword("pass123");

        User user = new User();
        user.setId(1L);
        user.setCodeUser("user1");
        user.setPassword("encodedPass");

        String token = "jwt-token-123";

        when(authService.authenticate(request)).thenReturn(user);
        when(tokenSecurity.generateToken(user)).thenReturn(token);

        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        ReflectionTestUtils.setField(controller, "cookieSecure", true);

        ResponseEntity<ApiResponse<Void>> response = controller.login(request, responseMock);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.message()).isEqualTo("Login realizado com sucesso");

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseMock).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookieValue = headerCaptor.getValue();
        assertThat(cookieValue).contains("token=" + token);
        assertThat(cookieValue).contains("HttpOnly");
        assertThat(cookieValue).contains("Secure");
        assertThat(cookieValue).contains("Path=/");
        assertThat(cookieValue).contains("Max-Age=3600");
        assertThat(cookieValue).contains("SameSite=Lax");
    }

    @Test
    void login_credenciaisInvalidas_lancaExcecao() {
        LoginRequest request = new LoginRequest();
        request.setCodeUser("user1");
        request.setPassword("wrong");

        when(authService.authenticate(request)).thenReturn(null);

        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> controller.login(request, responseMock))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Usuário ou senha inválidos");
        verify(responseMock, never()).addHeader(anyString(), anyString());
    }

    @Test
    void login_cookieNaoSeguro_naoContemSecure() {
        LoginRequest request = new LoginRequest();
        request.setCodeUser("user1");
        request.setPassword("pass123");

        User user = new User();
        user.setId(1L);
        user.setCodeUser("user1");
        user.setPassword("encodedPass");

        when(authService.authenticate(request)).thenReturn(user);
        when(tokenSecurity.generateToken(user)).thenReturn("token");

        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        ReflectionTestUtils.setField(controller, "cookieSecure", false);

        controller.login(request, responseMock);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseMock).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookieValue = headerCaptor.getValue();
        assertThat(cookieValue).doesNotContain("Secure");
    }

    @Test
    void logout_removeCookie() {
        ReflectionTestUtils.setField(controller, "cookieSecure", true);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        ResponseEntity<ApiResponse<Void>> response = controller.logout(responseMock);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.message()).isEqualTo("Logout realizado");

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseMock).addHeader(eq(HttpHeaders.SET_COOKIE), headerCaptor.capture());
        String cookieValue = headerCaptor.getValue();
        assertThat(cookieValue).contains("token=");
        assertThat(cookieValue).contains("Max-Age=0");
        assertThat(cookieValue).contains("HttpOnly");
        assertThat(cookieValue).contains("Secure");
        assertThat(cookieValue).contains("Path=/");
        assertThat(cookieValue).contains("SameSite=Lax");
    }
}
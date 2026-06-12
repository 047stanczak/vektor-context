package com.vektorcontext.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private String codeUser;

    @NotBlank
    private String password;

    public String getCodeUser() {
        return codeUser;
    }

    public void setCodeUser(String codeUser) {
        this.codeUser = codeUser;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
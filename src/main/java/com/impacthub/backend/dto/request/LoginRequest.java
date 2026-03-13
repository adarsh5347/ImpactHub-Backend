package com.impacthub.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @JsonAlias({"username", "userName", "login"})
    @NotBlank(message = "Email or username is required")
    private String email;

    @JsonAlias({"pass", "pwd"})
    @NotBlank(message = "Password is required")
    private String password;
}

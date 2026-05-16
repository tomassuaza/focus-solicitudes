package com.focus.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String idToken) { }

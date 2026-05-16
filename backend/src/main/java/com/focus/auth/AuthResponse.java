package com.focus.auth;

public record AuthResponse(String token, String email, String nombre, String rol) { }

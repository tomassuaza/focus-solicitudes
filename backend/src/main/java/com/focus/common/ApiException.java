package com.focus.common;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException notFound(String entidad, Object id) {
        return new ApiException(HttpStatus.NOT_FOUND, entidad + " no encontrado: " + id);
    }

    public static ApiException badRequest(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ApiException forbidden(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, mensaje);
    }
}

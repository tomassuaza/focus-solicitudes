package com.focus.clientes;

import com.focus.common.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
    @NotBlank @Size(max = 180) String nombre,
    @NotNull TipoCliente tipo) { }

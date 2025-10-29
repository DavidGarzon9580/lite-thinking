package com.litethinking.platform.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrdenRequest(
        @NotBlank(message = "El NIT de la empresa es obligatorio")
        String empresaNit,
        @Email(message = "Correo invalido")
        @NotBlank(message = "El correo del cliente es obligatorio")
        String clienteCorreo,
        @NotBlank(message = "El nombre del cliente es obligatorio")
        String clienteNombre,
        @NotEmpty(message = "Debe agregar al menos un producto")
        @Valid
        List<OrdenItemRequest> items
) {
}

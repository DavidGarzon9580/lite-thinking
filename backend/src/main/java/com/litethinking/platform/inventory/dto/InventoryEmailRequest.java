package com.litethinking.platform.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InventoryEmailRequest(
        @Email(message = "Correo invalido")
        @NotBlank(message = "El correo destino es obligatorio")
        String emailDestino
) {
}

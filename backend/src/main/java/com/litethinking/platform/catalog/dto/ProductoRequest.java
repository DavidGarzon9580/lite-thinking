package com.litethinking.platform.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductoRequest(
        @NotBlank(message = "El codigo es obligatorio")
        String codigo,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        String caracteristicas,
        @NotBlank(message = "Debe asociar una empresa")
        String empresaNit,
        @NotEmpty(message = "Debe registrar al menos un precio")
        @Valid
        List<PrecioRequest> precios,
        @NotNull(message = "Debe especificar las categorias (lista vacia si no aplica)")
        List<String> categorias
) {
}

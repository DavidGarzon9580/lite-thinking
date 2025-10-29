package com.litethinking.platform.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductoResponse(
        UUID id,
        String codigo,
        String nombre,
        String caracteristicas,
        String empresaNit,
        List<PrecioDto> precios,
        List<String> categorias
) {

    public record PrecioDto(String moneda, BigDecimal valor) {
    }
}

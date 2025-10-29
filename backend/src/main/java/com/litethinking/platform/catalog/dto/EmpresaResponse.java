package com.litethinking.platform.catalog.dto;

import java.util.List;

public record EmpresaResponse(
        String nit,
        String nombre,
        String direccion,
        String telefono,
        List<ProductoSummary> productos
) {
}

package com.litethinking.platform.catalog.mapper;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.dto.EmpresaRequest;
import com.litethinking.platform.catalog.dto.EmpresaResponse;
import com.litethinking.platform.catalog.dto.ProductoSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmpresaMapper {

    Empresa toEntity(EmpresaRequest request);

    default EmpresaResponse toResponse(Empresa empresa) {
        List<ProductoSummary> productos = empresa.getProductos().stream()
                .map(producto -> new ProductoSummary(producto.getId(), producto.getCodigo(), producto.getNombre()))
                .toList();
        return new EmpresaResponse(
                empresa.getNit(),
                empresa.getNombre(),
                empresa.getDireccion(),
                empresa.getTelefono(),
                productos
        );
    }
}

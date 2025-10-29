package com.litethinking.platform.catalog.mapper;

import com.litethinking.platform.catalog.domain.Categoria;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.domain.ProductoPrecio;
import com.litethinking.platform.catalog.dto.PrecioRequest;
import com.litethinking.platform.catalog.dto.ProductoRequest;
import com.litethinking.platform.catalog.dto.ProductoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductoMapper {

    default ProductoResponse toResponse(Producto producto) {
        List<ProductoResponse.PrecioDto> precios = producto.getPrecios().stream()
                .map(precio -> new ProductoResponse.PrecioDto(precio.getMoneda(), precio.getValor()))
                .toList();

        List<String> categorias = producto.getCategorias().stream()
                .map(Categoria::getNombre)
                .toList();

        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCaracteristicas(),
                producto.getEmpresa().getNit(),
                precios,
                categorias
        );
    }

    default Set<ProductoPrecio> toPrecioEntities(List<PrecioRequest> precios) {
        return precios.stream()
                .map(precio -> new ProductoPrecio(precio.moneda(), precio.valor()))
                .collect(Collectors.toSet());
    }
}

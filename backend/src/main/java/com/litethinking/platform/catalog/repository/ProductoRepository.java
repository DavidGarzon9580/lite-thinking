package com.litethinking.platform.catalog.repository;

import com.litethinking.platform.catalog.domain.Producto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    boolean existsByCodigoAndEmpresaNit(String codigo, String empresaNit);

    @EntityGraph(attributePaths = {"categorias", "precios"})
    List<Producto> findByEmpresaNit(String empresaNit);

    @EntityGraph(attributePaths = {"categorias", "precios"})
    Optional<Producto> findById(UUID id);
}

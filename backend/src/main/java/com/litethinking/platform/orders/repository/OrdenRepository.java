package com.litethinking.platform.orders.repository;

import com.litethinking.platform.orders.domain.Orden;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    @EntityGraph(attributePaths = {"cliente", "items", "items.producto", "empresa"})
    List<Orden> findByEmpresaNit(String empresaNit);
}

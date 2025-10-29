package com.litethinking.platform.catalog.repository;

import com.litethinking.platform.catalog.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {
}

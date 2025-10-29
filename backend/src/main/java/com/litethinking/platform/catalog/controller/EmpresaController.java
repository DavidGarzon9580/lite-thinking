package com.litethinking.platform.catalog.controller;

import com.litethinking.platform.catalog.dto.EmpresaRequest;
import com.litethinking.platform.catalog.dto.EmpresaResponse;
import com.litethinking.platform.catalog.dto.EmpresaUpdateRequest;
import com.litethinking.platform.catalog.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> listar() {
        return ResponseEntity.ok(empresaService.listar());
    }

    @GetMapping("/{nit}")
    public ResponseEntity<EmpresaResponse> obtener(@PathVariable String nit) {
        return ResponseEntity.ok(empresaService.obtener(nit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<EmpresaResponse> crear(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.crear(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{nit}")
    public ResponseEntity<EmpresaResponse> actualizar(@PathVariable String nit,
                                                      @Valid @RequestBody EmpresaUpdateRequest request) {
        return ResponseEntity.ok(empresaService.actualizar(nit, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{nit}")
    public ResponseEntity<Void> eliminar(@PathVariable String nit) {
        empresaService.eliminar(nit);
        return ResponseEntity.noContent().build();
    }
}

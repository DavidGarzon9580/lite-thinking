package com.litethinking.platform.catalog.service;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.dto.EmpresaRequest;
import com.litethinking.platform.catalog.dto.EmpresaResponse;
import com.litethinking.platform.catalog.dto.EmpresaUpdateRequest;
import com.litethinking.platform.catalog.mapper.EmpresaMapper;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Transactional
    public EmpresaResponse crear(EmpresaRequest request) {
        if (empresaRepository.existsById(request.nit())) {
            throw new ResourceAlreadyExistsException("La empresa ya existe");
        }

        Empresa empresa = empresaMapper.toEntity(request);
        Empresa saved = empresaRepository.save(empresa);
        return empresaMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtener(String nit) {
        Empresa empresa = empresaRepository.findById(nit)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        return empresaMapper.toResponse(empresa);
    }

    @Transactional
    public EmpresaResponse actualizar(String nit, EmpresaUpdateRequest request) {
        Empresa empresa = empresaRepository.findById(nit)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
        empresa.update(request.nombre(), request.direccion(), request.telefono());
        return empresaMapper.toResponse(empresa);
    }

    @Transactional
    public void eliminar(String nit) {
        if (!empresaRepository.existsById(nit)) {
            throw new ResourceNotFoundException("Empresa no encontrada");
        }
        empresaRepository.deleteById(nit);
    }
}

package com.litethinking.platform.catalog.service;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.dto.EmpresaRequest;
import com.litethinking.platform.catalog.dto.EmpresaResponse;
import com.litethinking.platform.catalog.mapper.EmpresaMapper;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void crearDebeGuardarEmpresaCuandoNitNoExiste() {
        EmpresaRequest request = new EmpresaRequest("123", "Lite", "Direccion", "555");
        Empresa entity = new Empresa("123", "Lite", "Direccion", "555");
        EmpresaResponse expected = new EmpresaResponse("123", "Lite", "Direccion", "555", java.util.List.of());

        when(empresaRepository.existsById("123")).thenReturn(false);
        when(empresaMapper.toEntity(request)).thenReturn(entity);
        when(empresaRepository.save(entity)).thenReturn(entity);
        when(empresaMapper.toResponse(entity)).thenReturn(expected);

        EmpresaResponse response = empresaService.crear(request);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void crearDebeLanzarExcepcionSiNitYaExiste() {
        EmpresaRequest request = new EmpresaRequest("123", "Lite", "Direccion", "555");
        when(empresaRepository.existsById("123")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.crear(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void obtenerDebeRetornarEmpresa() {
        Empresa empresa = new Empresa("123", "Lite", "Direccion", "555");
        EmpresaResponse response = new EmpresaResponse("123", "Lite", "Direccion", "555", java.util.List.of());
        when(empresaRepository.findById("123")).thenReturn(Optional.of(empresa));
        when(empresaMapper.toResponse(empresa)).thenReturn(response);

        assertThat(empresaService.obtener("123")).isEqualTo(response);
    }

    @Test
    void eliminarDebeFallarSiNoExiste() {
        when(empresaRepository.existsById("999")).thenReturn(false);

        assertThatThrownBy(() -> empresaService.eliminar("999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarDebeBorrarEmpresaExistente() {
        when(empresaRepository.existsById("123")).thenReturn(true);

        empresaService.eliminar("123");

        org.mockito.Mockito.verify(empresaRepository).deleteById("123");
    }
}

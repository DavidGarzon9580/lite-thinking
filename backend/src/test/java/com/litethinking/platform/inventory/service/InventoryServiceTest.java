package com.litethinking.platform.inventory.service;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import com.litethinking.platform.inventory.report.InventoryPdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private InventoryPdfGenerator pdfGenerator;
    @Mock
    private MailService mailService;
    @Mock
    private DocumentStorageService documentStorageService;

    @InjectMocks
    private InventoryService inventoryService;

    private Empresa empresa;

    @BeforeEach
    void setup() {
        empresa = new Empresa("123", "Lite", "Direccion", "555");
    }

    @Test
    void enviarInventarioDebeGenerarPdfGuardarYEnviarCorreo() {
        Producto producto = new Producto("CODE", "Producto", "Desc", empresa);
        byte[] pdf = new byte[]{1, 2, 3};

        when(empresaRepository.findById("123")).thenReturn(Optional.of(empresa));
        when(productoRepository.findByEmpresaNit("123")).thenReturn(List.of(producto));
        when(pdfGenerator.build(empresa, List.of(producto))).thenReturn(pdf);
        when(documentStorageService.storeInventoryPdf("123", pdf)).thenReturn("/tmp/inventory-123.pdf");

        inventoryService.enviarInventario("123", "correo@dominio.com");

        verify(pdfGenerator).build(empresa, List.of(producto));
        verify(documentStorageService).storeInventoryPdf("123", pdf);
        verify(mailService).sendInventoryEmail(eq("correo@dominio.com"), any(), any(), eq(pdf));
    }

    @Test
    void generarPdfDebeFallarCuandoEmpresaNoExiste() {
        when(empresaRepository.findById("404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.generarPdf("404"))
                .isInstanceOf(com.litethinking.platform.common.exception.ResourceNotFoundException.class);
    }
}

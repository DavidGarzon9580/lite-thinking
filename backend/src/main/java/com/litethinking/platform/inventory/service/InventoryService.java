package com.litethinking.platform.inventory.service;

import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.repository.EmpresaRepository;
import com.litethinking.platform.catalog.repository.ProductoRepository;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import com.litethinking.platform.inventory.report.InventoryPdfGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final EmpresaRepository empresaRepository;
    private final ProductoRepository productoRepository;
    private final InventoryPdfGenerator pdfGenerator;
    private final MailService mailService;
    private final DocumentStorageService documentStorageService;

    public InventoryService(EmpresaRepository empresaRepository,
                            ProductoRepository productoRepository,
                            InventoryPdfGenerator pdfGenerator,
                            MailService mailService,
                            DocumentStorageService documentStorageService) {
        this.empresaRepository = empresaRepository;
        this.productoRepository = productoRepository;
        this.pdfGenerator = pdfGenerator;
        this.mailService = mailService;
        this.documentStorageService = documentStorageService;
    }

    @Transactional(readOnly = true)
    public byte[] generarPdf(String empresaNit) {
        Empresa empresa = obtenerEmpresa(empresaNit);
        List<Producto> productos = productoRepository.findByEmpresaNit(empresaNit);
        return pdfGenerator.build(empresa, productos);
    }

    @Transactional(readOnly = true)
    public void enviarInventario(String empresaNit, String correoDestino) {
        Empresa empresa = obtenerEmpresa(empresaNit);
        byte[] pdf = generarPdf(empresaNit);
        String storageLocation = documentStorageService.storeInventoryPdf(empresaNit, pdf);
        String subject = "Inventario " + empresa.getNombre();
        String body = storageLocation != null
                ? "Adjunto encontraras el inventario actualizado. Copia de respaldo: " + storageLocation
                : "Adjunto encontraras el inventario actualizado.";
        mailService.sendInventoryEmail(correoDestino, subject, body, pdf);
    }

    private Empresa obtenerEmpresa(String nit) {
        return empresaRepository.findById(nit)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada"));
    }
}

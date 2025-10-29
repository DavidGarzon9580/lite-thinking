package com.litethinking.platform.inventory.controller;

import com.litethinking.platform.inventory.dto.InventoryEmailRequest;
import com.litethinking.platform.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{empresaNit}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String empresaNit) {
        byte[] pdf = inventoryService.generarPdf(empresaNit);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-" + empresaNit + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{empresaNit}/email")
    public ResponseEntity<Void> enviarPorCorreo(@PathVariable String empresaNit,
                                                @Valid @RequestBody InventoryEmailRequest request) {
        inventoryService.enviarInventario(empresaNit, request.emailDestino());
        return ResponseEntity.accepted().build();
    }
}

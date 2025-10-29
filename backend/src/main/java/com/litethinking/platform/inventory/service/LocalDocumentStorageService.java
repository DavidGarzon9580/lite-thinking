package com.litethinking.platform.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalDocumentStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalDocumentStorageService.class);
    private static final String OUTPUT_DIR = System.getProperty("java.io.tmpdir") + File.separator + "inventories";

    @Override
    public String storeInventoryPdf(String empresaNit, byte[] pdfContent) {
        try {
            Path directory = Path.of(OUTPUT_DIR);
            Files.createDirectories(directory);
            Path pdfPath = directory.resolve("inventory-" + empresaNit + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfPath.toFile())) {
                fos.write(pdfContent);
            }
            log.info("PDF de inventario almacenado localmente en {}", pdfPath);
            return pdfPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.warn("No fue posible almacenar el PDF localmente", e);
            return null;
        }
    }
}

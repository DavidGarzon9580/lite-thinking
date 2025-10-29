package com.litethinking.platform.inventory.service;

public interface DocumentStorageService {
    String storeInventoryPdf(String empresaNit, byte[] pdfContent);
}

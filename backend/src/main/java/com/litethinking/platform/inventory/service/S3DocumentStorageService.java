package com.litethinking.platform.inventory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Instant;

@Service
@Primary
@ConditionalOnProperty(prefix = "aws.s3", name = "enabled", havingValue = "true")
public class S3DocumentStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3DocumentStorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3DocumentStorageService(
            S3Client s3Client,
            @Value("${aws.s3.bucket}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String storeInventoryPdf(String empresaNit, byte[] pdfContent) {
        String key = "inventories/" + empresaNit + "/inventory-" + Instant.now().toEpochMilli() + ".pdf";
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(pdfContent));
        String objectUrl = "s3://" + bucketName + "/" + key;
        log.info("PDF de inventario almacenado en S3 {}", objectUrl);
        return objectUrl;
    }
}

package com.shopkeeper.app.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a barcode (Code128) encoding the bill payment reference so the
 * customer/shopkeeper can scan it at the counter (e.g. paired with a POS
 * scanner) to pull up and settle a bill instantly.
 */
@Service
@Slf4j
public class BarcodeService {

    @Value("${app.barcode.storage-path}")
    private String storagePath;

    /**
     * Encodes a payload (e.g. "BILL:INV-000123:AMT:499.00") into a Code128 barcode PNG.
     * @return relative file path of the saved barcode image
     */
    public String generateBarcode(String payload, String fileNamePrefix) {
        try {
            Path dir = Path.of(storagePath);
            Files.createDirectories(dir);

            BitMatrix matrix = new MultiFormatWriter().encode(payload, BarcodeFormat.CODE_128, 400, 150);
            String fileName = fileNamePrefix + "_" + System.currentTimeMillis() + ".png";
            Path filePath = dir.resolve(fileName);
            MatrixToImageWriter.writeToPath(matrix, "PNG", filePath);

            return filePath.toString();
        } catch (Exception e) {
            log.error("Failed to generate barcode: {}", e.getMessage());
            return null;
        }
    }

    public File getBarcodeFile(String path) {
        return new File(path);
    }
}

package com.cafeerp.service;

import com.cafeerp.config.UpiProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private static final int QR_SIZE = 300;

    private final UpiProperties upiProperties;

    public String buildUpiPaymentLink(BigDecimal amount, String transactionNote) {
        return "upi://pay?pa=" + encode(upiProperties.getId())
                + "&pn=" + encode(upiProperties.getPayeeName())
                + "&am=" + encode(amount.toPlainString())
                + "&tn=" + encode(transactionNote)
                + "&cu=" + encode(upiProperties.getCurrency());
    }

    public String generateQrCodeBase64Png(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Failed to generate UPI QR code", ex);
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

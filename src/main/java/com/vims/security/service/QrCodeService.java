package com.vims.security.service;

import io.nayuki.qrcodegen.QrCode;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

@Service
public class QrCodeService {
    public byte[] generateQrCode(String otpauthUrl) throws Exception {
        System.out.println("QrCodeService: Generating QR code for URL: " + otpauthUrl);
        
        // Generate QR code using the QRCode library with higher error correction
        QrCode qr = QrCode.encodeText(otpauthUrl, QrCode.Ecc.HIGH);
        System.out.println("QrCodeService: QR code size: " + qr.size + "x" + qr.size);
        
        // Create image with larger scale for better scanning
        int scale = 10; // Size of each module (increased from 8)
        int size = qr.size;
        int border = 4; // Add border for better scanning
        int imgSize = (size + 2 * border) * scale;
        
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill background with white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imgSize, imgSize);
        
        // Draw QR code modules with border
        g.setColor(Color.BLACK);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (qr.getModule(x, y)) {
                    g.fillRect((x + border) * scale, (y + border) * scale, scale, scale);
                }
            }
        }
        
        g.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        byte[] result = baos.toByteArray();
        System.out.println("QrCodeService: Generated QR code image: " + result.length + " bytes, " + imgSize + "x" + imgSize + " pixels");
        return result;
    }
}

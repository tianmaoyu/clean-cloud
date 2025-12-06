package org.clean.example.xing;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {
    
    /**
     * 生成二维码字节数组
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @param margin 边距
     * @return 二维码图片的字节数组
     */
    public static byte[] generateQRCode(String content, int width, int height, int margin) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin); // 设置边距
            
            BitMatrix matrix = new MultiFormatWriter().encode(
                content, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 转换为BufferedImage
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成无白边的二维码
     */
    public static byte[] generateQRCodeNoMargin(String content) {
        return generateQRCode(content, 50, 50, 0);
    }
    
    /**
     * 生成带自定义边距的二维码
     */
    public static byte[] generateQRCodeWithMargin(String content, int size, int margin) {
        return generateQRCode(content, size, size, margin);
    }
    public static byte[] generateBarcode(String content)  {
        return generateBarcode(content, 60, 12);
    }
    //生成条形码
    @SneakyThrows
    public static byte[] generateBarcode(String content, int width, int height)  {
        // 选择条形码格式，例如 CODE_128
        BarcodeFormat format = BarcodeFormat.CODE_128;
        // 设置编码提示
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 创建条形码矩阵
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, format, width, height, hints);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", baos);
        return baos.toByteArray();
    }

}
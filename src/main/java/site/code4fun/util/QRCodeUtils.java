package site.code4fun.util;

import java.io.ByteArrayOutputStream;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.EncodeHintType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class QRCodeUtils {
	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 300;
	private static final int MIN_SIZE = 100;
	private static final int MAX_SIZE = 1000;
	private static final String DEFAULT_FORMAT = "PNG";
	private static final int DEFAULT_MARGIN = 1;

	/**
	 * Tạo mã QR với kích thước mặc định
	 * @param text Nội dung cần mã hóa
	 * @return Mảng byte chứa hình ảnh QR code
	 */
	public static byte[] getQRCodeImage(String text) {
		return getQRCodeImage(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Tạo mã QR với kích thước tùy chỉnh
	 * @param text Nội dung cần mã hóa
	 * @param width Chiều rộng
	 * @param height Chiều cao
	 * @return Mảng byte chứa hình ảnh QR code
	 */
	public static byte[] getQRCodeImage(String text, int width, int height) {
		validateInput(text, width, height);
		
		try {
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

			Writer qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
			
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_FORMAT, byteArrayOutputStream);
				return byteArrayOutputStream.toByteArray();
			}
		} catch (WriterException e) {
			log.error("Error generating QR code: {}", e.getMessage(), e);
			throw new QRCodeGenerationException("Failed to generate QR code", e);
		} catch (Exception e) {
			log.error("Unexpected error while generating QR code: {}", e.getMessage(), e);
			throw new QRCodeGenerationException("Unexpected error while generating QR code", e);
		}
	}

	/**
	 * Tạo mã QR với các tùy chọn nâng cao
	 * @param text Nội dung cần mã hóa
	 * @param width Chiều rộng
	 * @param height Chiều cao
	 * @param errorCorrectionLevel Mức độ sửa lỗi
	 * @param margin Độ dày viền
	 * @return Mảng byte chứa hình ảnh QR code
	 */
	public static byte[] getQRCodeImage(String text, int width, int height, 
									  ErrorCorrectionLevel errorCorrectionLevel, int margin) {
		validateInput(text, width, height);
		validateAdvancedOptions(errorCorrectionLevel, margin);
		
		try {
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
			hints.put(EncodeHintType.MARGIN, margin);
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

			Writer qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
			
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
				MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_FORMAT, byteArrayOutputStream);
				return byteArrayOutputStream.toByteArray();
			}
		} catch (WriterException e) {
			log.error("Error generating QR code with advanced options: {}", e.getMessage(), e);
			throw new QRCodeGenerationException("Failed to generate QR code with advanced options", e);
		} catch (Exception e) {
			log.error("Unexpected error while generating QR code with advanced options: {}", e.getMessage(), e);
			throw new QRCodeGenerationException("Unexpected error while generating QR code with advanced options", e);
		}
	}

	private static void validateInput(String text, int width, int height) {
		if (!StringUtils.hasText(text)) {
			throw new IllegalArgumentException("QR code text cannot be null or empty");
		}
		if (width < MIN_SIZE || width > MAX_SIZE) {
			throw new IllegalArgumentException("Width must be between " + MIN_SIZE + " and " + MAX_SIZE);
		}
		if (height < MIN_SIZE || height > MAX_SIZE) {
			throw new IllegalArgumentException("Height must be between " + MIN_SIZE + " and " + MAX_SIZE);
		}
	}

	private static void validateAdvancedOptions(ErrorCorrectionLevel errorCorrectionLevel, int margin) {
		if (errorCorrectionLevel == null) {
			throw new IllegalArgumentException("Error correction level cannot be null");
		}
		if (margin < 0 || margin > 4) {
			throw new IllegalArgumentException("Margin must be between 0 and 4");
		}
	}

	public static class QRCodeGenerationException extends RuntimeException {
		public QRCodeGenerationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}

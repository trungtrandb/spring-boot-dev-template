package site.code4fun.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.OutputStream;

@NoArgsConstructor(access = AccessLevel.NONE)
@Component
@Lazy
@Slf4j
public class PdfUtils {

    private static final String DEFAULT_OUTPUT_FILE = "test.pdf";

    /**
     * Generate PDF file from HTML string
     * @param html HTML content to convert
     * @param outputPath Path to save PDF file, if null will use default path
     */
    @SneakyThrows 
    public static void generatePdfFromHtml(String html, String outputPath) {
        if (StringUtils.isEmpty(html)) {
            throw new IllegalArgumentException("HTML content cannot be empty");
        }

        String filePath = StringUtils.isEmpty(outputPath) ? DEFAULT_OUTPUT_FILE : outputPath;

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            log.info("PDF generated successfully at: {}", filePath);
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    /**
     * Generate PDF file from HTML string using default output path
     * @param html HTML content to convert
     */
    public static void generatePdfFromHtml(String html) {
        generatePdfFromHtml(html, null);
    }
}

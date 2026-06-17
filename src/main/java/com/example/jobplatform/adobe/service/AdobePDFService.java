package com.example.jobplatform.adobe.service;

import com.example.jobplatform.adobe.config.AdobeCredentialsProvider;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdobePDFService {

    @Autowired
    private AdobeCredentialsProvider credentialsProvider;

    @Autowired
    private AdobeAPIClient adobeAPIClient;

    @Value("${adobe.pdfservices.api.url:https://pdf-services-api.adobe.io}")
    private String adobeApiUrl;

    @Value("${adobe.pdf.extraction.enabled:true}")
    private boolean extractionEnabled;

    @Value("${adobe.client.timeout:30000}")
    private int clientTimeout;

    // "pdfbox" (default) = local extraction via Apache PDFBox
    // "adobe"            = real Adobe PDF Services API
    @Value("${adobe.pdf.extraction.provider:pdfbox}")
    private String extractionProvider;

    public String extractTextFromPDF(MultipartFile file) {
        if (!extractionEnabled) {
            throw new IllegalStateException("PDF extraction feature is currently disabled");
        }

        try {
            validatePDFFile(file);

            log.info("Starting PDF text extraction for file: {}", file.getOriginalFilename());
            log.info("File size: {} bytes", file.getSize());

            byte[] fileContent = file.getBytes();

            String extractedText;
            if ("adobe".equalsIgnoreCase(extractionProvider)) {
                extractedText = callAdobePDFExtractionAPI(fileContent, file.getOriginalFilename());
            } else {
                extractedText = extractTextWithPdfBox(fileContent);
            }

            extractedText = extractedText == null ? "" : extractedText.trim();
            if (extractedText.isBlank()) {
                log.warn("No text extracted from PDF. If this is a scanned image PDF, you will need OCR.");
            }

            log.info("PDF text extraction completed. Extracted {} characters", extractedText.length());
            return extractedText;

        } catch (IllegalArgumentException e) {
            log.error("File validation failed", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract text from PDF: {}", e.getMessage(), e);
            throw new RuntimeException(
                String.format("Failed to extract text from PDF '%s': %s", file.getOriginalFilename(), e.getMessage()), e
            );
        }
    }

    private void validatePDFFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or null");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean looksLikePdfContentType = contentType != null && contentType.toLowerCase().contains("pdf");
        boolean looksLikePdfFilename = filename != null && filename.toLowerCase().endsWith(".pdf");
        if (!looksLikePdfContentType && !looksLikePdfFilename) {
            throw new IllegalArgumentException(String.format(
                "Invalid file type: %s. Only PDF files are supported (content-type should contain 'pdf' or filename should end with .pdf)",
                contentType
            ));
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException(
                String.format("File size (%d bytes) exceeds maximum allowed size (50MB)", file.getSize())
            );
        }
    }

    private String callAdobePDFExtractionAPI(byte[] fileContent, String filename) {
        log.info("Step 1/3: Uploading PDF to Adobe PDF Services...");
        AdobeAPIClient.UploadResponse upload = adobeAPIClient.uploadPDF(fileContent, filename);
        log.info("  Upload complete. Asset ID: {}...", safePrefix(upload.assetID, 20));

        log.info("Step 2/3: Waiting for Adobe to process PDF (up to 10 min)...");
        adobeAPIClient.pollForCompletion(upload.assetID, upload.pollURL);
        log.info("  Processing complete.");

        log.info("Step 3/3: Downloading extracted text from Adobe...");
        String text = adobeAPIClient.downloadExtractedText(upload.assetID);
        log.info("  Downloaded {} characters.", text.length());

        return text;
    }

    private String extractTextWithPdfBox(byte[] fileContent) {
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract text with PDFBox: " + e.getMessage(), e);
        }
    }

    private String safePrefix(String s, int len) {
        if (s == null) {
            return "<null>";
        }
        return s.length() <= len ? s : s.substring(0, len);
    }
}

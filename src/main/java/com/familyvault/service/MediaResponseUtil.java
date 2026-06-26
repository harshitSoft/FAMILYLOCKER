package com.familyvault.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;

final class MediaResponseUtil {
    private MediaResponseUtil() {}

    static ResponseEntity<ByteArrayResource> file(byte[] bytes, String contentType, String fileName, boolean inline) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (contentType != null && !contentType.isBlank()) mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        String encoded = URLEncoder.encode(fileName == null ? "download" : fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = inline ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + (fileName == null ? "download" : fileName.replace("\"", "'")) + "\"; filename*=UTF-8''" + encoded)
                .body(new ByteArrayResource(bytes));
    }
}

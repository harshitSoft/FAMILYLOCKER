package com.familyvault.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.familyvault.exception.ApiException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryStorageService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);
    private final Cloudinary cloudinary;
    private final String folderRoot;

    public CloudinaryStorageService(Cloudinary cloudinary,
                                    @Value("${cloudinary.folder-root:digital-virasat}") String folderRoot) {
        this.cloudinary = cloudinary;
        this.folderRoot = cleanFolder(folderRoot);
    }

    public StoredCloudFile uploadFile(MultipartFile file, String folder) {
        try {
            return uploadBytes(file.getBytes(), file.getOriginalFilename(), file.getContentType(), file.getSize(), folder);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary upload failed");
        }
    }

    public StoredCloudFile uploadBytes(byte[] bytes, String originalFileName, String contentType, long sizeBytes, String folder) {
        try {
            String safeName = safeName(originalFileName);
            String publicId = UUID.randomUUID() + "-" + stripExtension(safeName);
            Map<?, ?> result;
            try {
                result = uploadToCloudinary(bytes, cloudFolder(folder), publicId, "auto");
            } catch (Exception autoFailure) {
                log.warn("Cloudinary auto resource detection failed for folder={}, publicId={}; retrying as raw: {}",
                        cloudFolder(folder), publicId, autoFailure.getMessage());
                result = uploadToCloudinary(bytes, cloudFolder(folder), publicId, "raw");
            }
            return storedFileFromResult(result, safeName, contentType, sizeBytes);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary upload failed");
        }
    }

    public byte[] downloadBytes(String secureUrl) {
        if (secureUrl == null || secureUrl.isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Cloudinary file URL is missing");
        }
        try (InputStream in = URI.create(secureUrl).toURL().openStream()) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Cloudinary file not found");
        }
    }

    public void deleteFile(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", resourceType == null || resourceType.isBlank() ? "raw" : resourceType
            ));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary delete failed");
        }
    }

    public String generatePreviewUrl(String publicId, String resourceType, String secureUrl) {
        if (secureUrl != null && !secureUrl.isBlank()) return secureUrl;
        return generateUrl(publicId, resourceType);
    }

    public String generateDownloadUrl(String publicId, String resourceType, String secureUrl) {
        String url = generateUrl(publicId, resourceType);
        return url.isBlank() ? secureUrl : url;
    }

    private String generateUrl(String publicId, String resourceType) {
        if (publicId == null || publicId.isBlank()) return "";
        return cloudinary.url()
                .secure(true)
                .resourceType(resourceType == null || resourceType.isBlank() ? "raw" : resourceType)
                .generate(publicId);
    }

    private String cloudFolder(String folder) {
        String cleaned = cleanFolder(folder);
        return cleaned.isBlank() ? folderRoot : folderRoot + "/" + cleaned;
    }

    private String cleanFolder(String folder) {
        if (folder == null) return "";
        return folder.replace("\\", "/").replaceAll("^/+|/+$", "").replaceAll("[^A-Za-z0-9_./-]", "-");
    }

    private String safeName(String name) {
        String value = name == null || name.isBlank() ? "file" : name;
        return value.replaceAll("[\\\\/\\r\\n]", "_");
    }

    private String stripExtension(String name) {
        String stripped = name.replaceAll("\\.[A-Za-z0-9]{1,12}$", "");
        stripped = stripped.replaceAll("[^A-Za-z0-9_-]", "-");
        return stripped.isBlank() ? "file" : stripped;
    }

    private String value(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    private Map<?, ?> uploadToCloudinary(byte[] bytes, String folder, String publicId, String resourceType) throws Exception {
        return cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "resource_type", resourceType,
                "folder", folder,
                "public_id", publicId,
                "use_filename", false,
                "overwrite", false
        ));
    }

    private StoredCloudFile storedFileFromResult(Map<?, ?> result, String safeName, String contentType, long sizeBytes) {
        String resourceType = value(result.get("resource_type"), "raw");
        String secureUrl = value(result.get("secure_url"), "");
        String storedPublicId = value(result.get("public_id"), "");
        if (secureUrl.isBlank() || storedPublicId.isBlank()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cloudinary did not return file metadata");
        }
        return new StoredCloudFile(storedPublicId, secureUrl, resourceType, safeName,
                contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType, sizeBytes);
    }

    public record StoredCloudFile(String publicId, String secureUrl, String resourceType,
                                  String originalFileName, String contentType, long sizeBytes) {}
}

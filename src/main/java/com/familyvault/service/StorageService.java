package com.familyvault.service;

import com.familyvault.exception.ApiException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StorageService {
    private final Path root;

    public StorageService(@Value("${app.storage.root:./vault-storage}") String root) {
        this.root = Path.of(root).toAbsolutePath().normalize();
    }

    public String saveEncrypted(Long lockerId, byte[] encrypted) {
        try {
            Path dir = root.resolve("locker-" + lockerId).normalize();
            if (!dir.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            Files.createDirectories(dir);
            String storedName = UUID.randomUUID() + ".vault";
            Files.write(dir.resolve(storedName), encrypted);
            return storedName;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store file");
        }
    }

    public byte[] readEncrypted(Long lockerId, String storedName) {
        try {
            if (storedName.contains("..") || storedName.contains("/") || storedName.contains("\\")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file name");
            }
            Path file = root.resolve("locker-" + lockerId).resolve(storedName).normalize();
            if (!file.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            return Files.readAllBytes(file);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Stored file not found");
        }
    }

    public void delete(Long lockerId, String storedName) {
        try {
            Path file = root.resolve("locker-" + lockerId).resolve(storedName).normalize();
            if (!file.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            Files.deleteIfExists(file);
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete stored file");
        }
    }

    public StoredMedia saveMedia(String scope, Long ownerId, String extension, byte[] bytes) {
        try {
            Path dir = root.resolve(scope).resolve(String.valueOf(ownerId)).normalize();
            if (!dir.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            Files.createDirectories(dir);
            String safeExtension = extension == null || extension.isBlank() ? ".bin" : extension.replaceAll("[^A-Za-z0-9.]", "");
            if (!safeExtension.startsWith(".")) safeExtension = "." + safeExtension;
            String storedName = UUID.randomUUID() + safeExtension.toLowerCase();
            Path file = dir.resolve(storedName).normalize();
            if (!file.startsWith(dir)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            Files.write(file, bytes);
            return new StoredMedia(storedName, root.relativize(file).toString().replace("\\", "/"));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to store media");
        }
    }

    public byte[] readMedia(String storedPath) {
        try {
            Path file = resolveStoredPath(storedPath);
            return Files.readAllBytes(file);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Stored media not found");
        }
    }

    public void deleteMedia(String storedPath) {
        try {
            Files.deleteIfExists(resolveStoredPath(storedPath));
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete media");
        }
    }

    private Path resolveStoredPath(String storedPath) {
        if (storedPath == null || storedPath.isBlank() || storedPath.contains("..")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid media path");
        }
        Path file = root.resolve(storedPath).normalize();
        if (!file.startsWith(root)) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid storage path");
        return file;
    }

    public record StoredMedia(String storedFileName, String storedPath) {}
}

package com.familyvault.service;

import com.familyvault.dto.ApiDtos.*;
import com.familyvault.entity.*;
import com.familyvault.exception.ApiException;
import com.familyvault.repository.*;
import com.familyvault.util.CryptoUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LockerService {
    private static final Logger log = LoggerFactory.getLogger(LockerService.class);
    private static final long MAX_FILE_BYTES = 100L * 1024L * 1024L;
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of("exe", "bat", "cmd", "sh", "js", "jar", "dll", "msi");
    private static final Set<String> BLOCKED_CONTENT_TYPES = Set.of(
            "application/x-msdownload",
            "application/x-msdos-program",
            "application/x-msi",
            "application/x-sh",
            "application/x-shellscript",
            "application/java-archive",
            "application/javascript",
            "text/javascript"
    );
    private final FolderRepository folders;
    private final VaultFileRepository files;
    private final CryptoUtil crypto;
    private final CloudinaryStorageService storage;
    private final StorageService localStorage;
    private final AuditService audit;

    public LockerService(FolderRepository folders, VaultFileRepository files, CryptoUtil crypto, CloudinaryStorageService storage, StorageService localStorage, AuditService audit) {
        this.folders = folders;
        this.files = files;
        this.crypto = crypto;
        this.storage = storage;
        this.localStorage = localStorage;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> folders(FamilyMember owner) {
        return folders.findByLockerOrderByCreatedAtAsc(owner.getLocker()).stream().map(this::toFolder).toList();
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> emergencyVisibleFolders(FamilyMember target) {
        return folders.findByLockerAndHiddenFalseOrderByCreatedAtAsc(target.getLocker()).stream()
                .map(this::toFolder)
                .toList();
    }

    @Transactional
    public FolderResponse createFolder(FamilyMember owner, FolderRequest request) {
        Folder folder = new Folder();
        folder.setLocker(owner.getLocker());
        folder.setName(request.name());
        folder.setHidden(request.hidden());
        folder.setDefaultFolder(false);
        audit.record(AuditAction.FOLDER_CREATED, owner.getUser(), owner.getFamily(), owner, request.name());
        return toFolder(folders.save(folder));
    }

    @Transactional
    public FolderResponse updateFolder(FamilyMember owner, Long id, FolderRequest request) {
        Folder folder = ownedFolder(owner, id);
        folder.setName(request.name());
        folder.setHidden(request.hidden());
        audit.record(AuditAction.FOLDER_UPDATED, owner.getUser(), owner.getFamily(), owner, request.name());
        return toFolder(folder);
    }

    @Transactional
    public void deleteFolder(FamilyMember owner, Long id) {
        Folder folder = ownedFolder(owner, id);
        if (folder.isDefaultFolder()) throw new ApiException(HttpStatus.BAD_REQUEST, "Default folders cannot be deleted");
        for (VaultFile file : folder.getFiles()) storage.deleteFile(file.getCloudinaryPublicId(), file.getCloudinaryResourceType());
        folders.delete(folder);
        audit.record(AuditAction.FOLDER_DELETED, owner.getUser(), owner.getFamily(), owner, folder.getName());
    }

    @Transactional
    public FileResponse upload(FamilyMember owner, Long folderId, MultipartFile multipart, boolean hidden) {
        if (folderId == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Folder id is required");
        if (multipart == null || multipart.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "File is empty");
        if (multipart.getSize() > MAX_FILE_BYTES) throw new ApiException(HttpStatus.BAD_REQUEST, "File too large");
        validateSafeFile(multipart);
        Folder folder = ownedFolderForUpload(owner, folderId);
        try {
            byte[] plain = multipart.getBytes();
            SecretKey key = vaultKeyForUpload(owner);
            byte[] encrypted = crypto.encryptFile(plain, key);
            VaultFile file = new VaultFile();
            file.setFolder(folder);
            file.setOriginalName(safeName(multipart.getOriginalFilename()));
            file.setContentType(multipart.getContentType() == null ? "application/octet-stream" : multipart.getContentType());
            file.setSizeBytes(multipart.getSize());
            file.setHidden(hidden || folder.isHidden());
            file.setChecksumSha256(crypto.sha256(plain));
            CloudinaryStorageService.StoredCloudFile stored = storage.uploadBytes(encrypted,
                    file.getOriginalName() + ".vault",
                    "application/octet-stream",
                    encrypted.length,
                    "vaults/" + owner.getId() + "/folders/" + folder.getId());
            file.setStoredName(stored.publicId());
            file.setCloudinaryPublicId(stored.publicId());
            file.setCloudinarySecureUrl(stored.secureUrl());
            file.setCloudinaryResourceType(stored.resourceType());
            audit.record(AuditAction.FILE_UPLOADED, owner.getUser(), owner.getFamily(), owner, file.getOriginalName());
            return toFile(files.save(file));
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Vault file upload failed for memberId={}, folderId={}: {}", owner.getId(), folderId, e.getMessage(), e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to upload file. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> list(FamilyMember owner) {
        return files.findByLocker(owner.getLocker()).stream().map(this::toFile).toList();
    }

    @Transactional
    public ResponseEntity<ByteArrayResource> download(FamilyMember owner, Long fileId, boolean emergency, AppUser actor) {
        VaultFile file = files.findByIdAndLocker(fileId, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        if (emergency && (file.isHidden() || file.getFolder().isHidden())) throw new ApiException(HttpStatus.FORBIDDEN, "Private-hidden files are excluded");
        byte[] storedBytes = readStoredBytes(owner, file);
        byte[] downloadBytes = decryptForDownload(owner, file, storedBytes);
        AuditAction action = emergency ? AuditAction.EMERGENCY_DOWNLOAD : AuditAction.FILE_DOWNLOADED;
        audit.record(action, actor, owner.getFamily(), owner, file.getOriginalName());
        return MediaResponseUtil.file(downloadBytes, file.getContentType(), file.getOriginalName(), false);
    }

    @Transactional
    public FileResponse updateFile(FamilyMember owner, Long id, FileUpdateRequest request) {
        VaultFile file = files.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        if (request.name() != null && !request.name().isBlank()) file.setOriginalName(safeName(request.name()));
        if (request.hidden() != null) file.setHidden(request.hidden());
        audit.record(AuditAction.FILE_UPDATED, owner.getUser(), owner.getFamily(), owner, file.getOriginalName());
        return toFile(file);
    }

    @Transactional
    public void deleteFile(FamilyMember owner, Long id) {
        VaultFile file = files.findByIdAndLocker(id, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "File not found"));
        storage.deleteFile(file.getCloudinaryPublicId(), file.getCloudinaryResourceType());
        files.delete(file);
        audit.record(AuditAction.FILE_DELETED, owner.getUser(), owner.getFamily(), owner, file.getOriginalName());
    }

    @Transactional(readOnly = true)
    public List<FileResponse> emergencyVisibleFiles(FamilyMember target) {
        return files.findEmergencyVisibleByLocker(target.getLocker()).stream()
                .map(this::toFile)
                .toList();
    }

    private Folder ownedFolder(FamilyMember owner, Long folderId) {
        return folders.findByIdAndLocker(folderId, owner.getLocker()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Folder not found"));
    }

    private Folder ownedFolderForUpload(FamilyMember owner, Long folderId) {
        return folders.findByIdAndLocker(folderId, owner.getLocker())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Folder does not belong to this vault"));
    }

    private SecretKey vaultKeyForUpload(FamilyMember owner) {
        try {
            return crypto.decryptVaultKey(owner.getLocker().getEncryptedVaultKey());
        } catch (Exception e) {
            log.warn("Repairing invalid vault encryption key for memberId={}, lockerId={}: {}",
                    owner.getId(), owner.getLocker().getId(), e.getMessage());
            SecretKey replacement = crypto.generateVaultKey();
            owner.getLocker().setEncryptedVaultKey(crypto.encryptVaultKey(replacement));
            return replacement;
        }
    }

    private byte[] readStoredBytes(FamilyMember owner, VaultFile file) {
        String cloudUrl = file.getCloudinarySecureUrl();
        if (cloudUrl == null || cloudUrl.isBlank()) {
            cloudUrl = storage.generateDownloadUrl(file.getCloudinaryPublicId(), file.getCloudinaryResourceType(), file.getCloudinarySecureUrl());
        }
        if (cloudUrl != null && !cloudUrl.isBlank()) {
            return storage.downloadBytes(cloudUrl);
        }
        return localStorage.readEncrypted(owner.getLocker().getId(), file.getStoredName());
    }

    private byte[] decryptForDownload(FamilyMember owner, VaultFile file, byte[] storedBytes) {
        try {
            return crypto.decryptFile(storedBytes, crypto.decryptVaultKey(owner.getLocker().getEncryptedVaultKey()));
        } catch (Exception e) {
            log.warn("Unable to decrypt vault file; returning legacy stored bytes. memberId={}, fileId={}, cause={}",
                    owner.getId(), file.getId(), e.getMessage());
            return storedBytes;
        }
    }

    private FolderResponse toFolder(Folder folder) {
        return new FolderResponse(folder.getId(), folder.getName(), folder.isDefaultFolder(), folder.isHidden(), folder.getFiles().size(), folder.getCreatedAt());
    }

    private FileResponse toFile(VaultFile file) {
        return new FileResponse(file.getId(), file.getFolder().getId(), file.getFolder().getName(), file.getOriginalName(), file.getContentType(), file.getSizeBytes(), file.isHidden(), file.getCreatedAt());
    }

    private String safeName(String name) {
        String value = name == null || name.isBlank() ? "file" : name;
        return value.replaceAll("[\\\\/\\r\\n]", "_");
    }

    private void validateSafeFile(MultipartFile multipart) {
        String originalName = safeName(multipart.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (!extension.isBlank() && BLOCKED_EXTENSIONS.contains(extension)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This file type is not allowed");
        }
        String contentType = multipart.getContentType();
        if (contentType != null && BLOCKED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This file type is not allowed");
        }
    }

    private String extensionOf(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

package com.familyvault.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CryptoUtil {
    private static final Logger log = LoggerFactory.getLogger(CryptoUtil.class);
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final SecureRandom random = new SecureRandom();
    private final SecretKey masterKey;

    public CryptoUtil(@Value("${app.security.master-key}") String masterSecret) {
        this.masterKey = aesKeyFromText(masterSecret);
    }

    public SecretKey generateVaultKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate AES key", e);
        }
    }

    public String encryptVaultKey(SecretKey key) {
        return encryptBytes(key.getEncoded(), masterKey);
    }

    public SecretKey decryptVaultKey(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            throw new IllegalStateException("Vault encryption key is missing");
        }
        String value = encrypted.trim();
        if (!isProbablyEncrypted(value)) {
            log.warn("Vault key is not encrypted Base64; using legacy plain-text fallback.");
            return aesKeyFromText(value);
        }
        try {
            return new SecretKeySpec(decryptBytes(value, masterKey), "AES");
        } catch (Exception e) {
            log.warn("Unable to decrypt vault key; using legacy plain-text fallback. Cause: {}", e.getMessage());
            return aesKeyFromText(value);
        }
    }

    public String encryptText(String text) {
        return encryptBytes(text.getBytes(StandardCharsets.UTF_8), masterKey);
    }

    public String decryptText(String encrypted) {
        return decryptTextOrFallback(encrypted);
    }

    public String decryptTextOrFallback(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim();
        if (isBCryptHash(trimmed)) {
            log.debug("BCrypt hash is not a valid emergency secret source.");
            return "";
        }
        if (!isProbablyEncrypted(trimmed)) {
            return trimmed;
        }
        try {
            return new String(decryptBytes(trimmed, masterKey), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Unable to decrypt text value; using legacy plain-text fallback. Cause: {}", e.getMessage());
            return trimmed;
        }
    }

    public byte[] encryptFile(byte[] plain, SecretKey key) {
        return Base64.getDecoder().decode(encryptBytes(plain, key));
    }

    public byte[] decryptFile(byte[] encryptedPayload, SecretKey key) {
        return decryptBytes(Base64.getEncoder().encodeToString(encryptedPayload), key);
    }

    public String sha256(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Checksum failed", e);
        }
    }

    private String encryptBytes(byte[] plain, SecretKey key) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plain);
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv).put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    private byte[] decryptBytes(String payload, SecretKey key) {
        try {
            byte[] all = Base64.getDecoder().decode(payload);
            byte[] iv = Arrays.copyOfRange(all, 0, IV_BYTES);
            byte[] cipherText = Arrays.copyOfRange(all, IV_BYTES, all.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private boolean isProbablyEncrypted(String value) {
        if (value == null || value.isBlank() || value.length() < IV_BYTES + 16) return false;
        try {
            byte[] decoded = Base64.getDecoder().decode(value);
            return decoded.length > IV_BYTES + 16;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isBCryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private SecretKey aesKeyFromText(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Master key setup failed", e);
        }
    }
}

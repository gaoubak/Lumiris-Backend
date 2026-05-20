package com.minoh.lumiris_backend.storage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api/storage")
@Validated
public class StorageController {

    private static final Set<String> ALLOWED_BUCKETS = Set.of("uploads", "assets", "backups");
    private static final Duration UPLOAD_TTL = Duration.ofMinutes(15);
    private static final Duration DOWNLOAD_TTL = Duration.ofHours(1);

    private final StorageService storage;
    private final StorageProperties props;

    public StorageController(StorageService storage, StorageProperties props) {
        this.storage = storage;
        this.props = props;
    }

    public record UploadUrlRequest(
            @NotBlank @Pattern(regexp = "uploads|assets|backups") String bucket,
            @NotBlank String key,
            String contentType) {}

    @PostMapping("/upload-url")
    public StorageService.PresignedUploadResult uploadUrl(@Valid @RequestBody UploadUrlRequest req) {
        return storage.generatePresignedUploadUrl(resolveBucket(req.bucket()), req.key(), UPLOAD_TTL, req.contentType());
    }

    @GetMapping("/download-url")
    public StorageService.PresignedDownloadResult downloadUrl(
            @RequestParam @Pattern(regexp = "uploads|assets|backups") String bucket,
            @RequestParam @NotBlank String key) {
        return storage.generatePresignedDownloadUrl(resolveBucket(bucket), key, DOWNLOAD_TTL);
    }

    private String resolveBucket(String alias) {
        if (!ALLOWED_BUCKETS.contains(alias)) {
            throw new IllegalArgumentException("Unknown bucket alias: " + alias);
        }
        return switch (alias) {
            case "uploads" -> props.buckets().uploads();
            case "assets" -> props.buckets().assets();
            case "backups" -> props.buckets().backups();
            default -> throw new IllegalArgumentException("Unknown bucket alias: " + alias);
        };
    }
}

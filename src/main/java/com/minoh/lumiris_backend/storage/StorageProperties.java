package com.minoh.lumiris_backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.s3")
public record StorageProperties(
        String endpoint,
        String region,
        String accessKeyId,
        String secretAccessKey,
        boolean pathStyleAccess,
        Buckets buckets
) {
    public record Buckets(String uploads, String assets, String backups) {}
}

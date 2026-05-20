package com.minoh.lumiris_backend.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageServiceTest {

    private MinioClient minio;
    private StorageService service;

    @BeforeEach
    void setup() {
        minio = mock(MinioClient.class);
        StorageProperties props = new StorageProperties(
                "http://localhost:9000",
                "eu-central-1",
                "access",
                "secret",
                true,
                new StorageProperties.Buckets("lumiris-uploads", "lumiris-assets", "lumiris-backups"));
        service = new StorageService(minio, props);
    }

    @Test
    void presignedUploadUrl_returnsUrlAndHeaders() throws Exception {
        when(minio.getPresignedObjectUrl(ArgumentMatchers.any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio.test/uploads/foo?sig=x");

        var result = service.generatePresignedUploadUrl(
                "lumiris-uploads", "user/123/photo.jpg", Duration.ofMinutes(15), "image/jpeg");

        assertThat(result.url()).startsWith("http://minio.test/");
        assertThat(result.headers()).containsEntry("Content-Type", "image/jpeg");
        assertThat(result.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void presignedDownloadUrl_returnsUrl() throws Exception {
        when(minio.getPresignedObjectUrl(ArgumentMatchers.any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://minio.test/uploads/foo?sig=y");

        var result = service.generatePresignedDownloadUrl(
                "lumiris-uploads", "user/123/photo.jpg", Duration.ofHours(1));

        assertThat(result.url()).startsWith("http://minio.test/");
        assertThat(result.expiresInSeconds()).isEqualTo(3600);
    }

    @Test
    void invalidKey_pathTraversal_rejected() {
        assertThatThrownBy(() -> service.generatePresignedUploadUrl(
                "lumiris-uploads", "../etc/passwd", Duration.ofMinutes(15), null))
                .isInstanceOf(StorageException.InvalidKey.class);
    }

    @Test
    void invalidKey_controlChars_rejected() {
        String badKey = "evil" + (char) 0x01 + "key";
        assertThatThrownBy(() -> service.generatePresignedDownloadUrl(
                "lumiris-uploads", badKey, Duration.ofMinutes(15)))
                .isInstanceOf(StorageException.InvalidKey.class);
    }

    @Test
    void blankKey_rejected() {
        assertThatThrownBy(() -> service.deleteFile("lumiris-uploads", " "))
                .isInstanceOf(StorageException.InvalidKey.class);
    }
}

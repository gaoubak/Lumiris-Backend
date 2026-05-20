package com.minoh.lumiris_backend.storage;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageHealthIndicatorTest {

    private MinioClient minio;
    private StorageProperties props;

    @BeforeEach
    void setup() {
        minio = mock(MinioClient.class);
        props = new StorageProperties(
                "http://localhost:9000",
                "eu-central-1",
                "access",
                "secret",
                true,
                new StorageProperties.Buckets("lumiris-uploads", "lumiris-assets", "lumiris-backups"));
    }

    @Test
    void up_whenBucketExists() throws Exception {
        when(minio.bucketExists(ArgumentMatchers.any(BucketExistsArgs.class))).thenReturn(true);
        Health h = new StorageHealthIndicator(minio, props).health();
        assertThat(h.getStatus()).isEqualTo(Status.UP);
        assertThat(h.getDetails()).containsEntry("bucket", "lumiris-uploads");
        assertThat(h.getDetails()).containsKey("latencyMs");
    }

    @Test
    void down_whenBucketMissing() throws Exception {
        when(minio.bucketExists(ArgumentMatchers.any(BucketExistsArgs.class))).thenReturn(false);
        Health h = new StorageHealthIndicator(minio, props).health();
        assertThat(h.getStatus()).isEqualTo(Status.DOWN);
        assertThat(h.getDetails()).containsEntry("reason", "bucket missing");
    }

    @Test
    void down_whenClientThrows() throws Exception {
        when(minio.bucketExists(ArgumentMatchers.any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("connection refused"));
        Health h = new StorageHealthIndicator(minio, props).health();
        assertThat(h.getStatus()).isEqualTo(Status.DOWN);
        assertThat(h.getDetails()).containsEntry("endpoint", "http://localhost:9000");
    }
}

package com.minoh.lumiris_backend.storage;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("storage")
public class StorageHealthIndicator implements HealthIndicator {

    private final MinioClient minio;
    private final StorageProperties props;

    public StorageHealthIndicator(MinioClient minio, StorageProperties props) {
        this.minio = minio;
        this.props = props;
    }

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        try {
            boolean exists = minio.bucketExists(BucketExistsArgs.builder()
                    .bucket(props.buckets().uploads())
                    .build());
            long latency = System.currentTimeMillis() - start;
            if (!exists) {
                return Health.down()
                        .withDetail("endpoint", props.endpoint())
                        .withDetail("bucket", props.buckets().uploads())
                        .withDetail("reason", "bucket missing")
                        .build();
            }
            return Health.up()
                    .withDetail("endpoint", props.endpoint())
                    .withDetail("bucket", props.buckets().uploads())
                    .withDetail("latencyMs", latency)
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("endpoint", props.endpoint())
                    .build();
        }
    }
}

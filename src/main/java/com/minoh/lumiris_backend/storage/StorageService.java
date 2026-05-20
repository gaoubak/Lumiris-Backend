package com.minoh.lumiris_backend.storage;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);
    private static final Pattern INVALID_KEY = Pattern.compile(".*(\\.\\./|\\.\\.\\\\|[\\x00-\\x1f]).*");

    private final MinioClient minio;
    private final StorageProperties props;

    public StorageService(MinioClient minio, StorageProperties props) {
        this.minio = minio;
        this.props = props;
    }

    public String uploadFile(String bucket, String key, byte[] content, String contentType) {
        validateKey(key);
        try (var stream = new ByteArrayInputStream(content)) {
            var resp = minio.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(stream, content.length, -1)
                    .contentType(contentType)
                    .build());
            log.info("uploaded bucket={} key={} etag={}", bucket, key, resp.etag());
            return resp.etag();
        } catch (Exception e) {
            throw new StorageException.UploadFailed(bucket, key, e);
        }
    }

    public byte[] downloadFile(String bucket, String key) {
        validateKey(key);
        try (var stream = minio.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("Download failed: " + bucket + "/" + key, e);
        }
    }

    public PresignedUploadResult generatePresignedUploadUrl(String bucket, String key, Duration expiry, String contentType) {
        validateKey(key);
        try {
            var extraHeaders = new HashMap<String, String>();
            if (contentType != null) extraHeaders.put("Content-Type", contentType);
            String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) expiry.toSeconds())
                    .extraHeaders(extraHeaders)
                    .build());
            return new PresignedUploadResult(url, extraHeaders, expiry.toSeconds());
        } catch (Exception e) {
            throw new StorageException("Presign upload failed", e);
        }
    }

    public PresignedDownloadResult generatePresignedDownloadUrl(String bucket, String key, Duration expiry) {
        validateKey(key);
        try {
            String url = minio.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) expiry.toSeconds())
                    .build());
            return new PresignedDownloadResult(url, expiry.toSeconds());
        } catch (Exception e) {
            throw new StorageException("Presign download failed", e);
        }
    }

    public void deleteFile(String bucket, String key) {
        validateKey(key);
        try {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
            log.info("deleted bucket={} key={}", bucket, key);
        } catch (Exception e) {
            throw new StorageException("Delete failed: " + bucket + "/" + key, e);
        }
    }

    public List<StoredObject> listFiles(String bucket, String prefix) {
        try {
            var args = ListObjectsArgs.builder().bucket(bucket);
            if (prefix != null && !prefix.isBlank()) args.prefix(prefix);
            var results = new ArrayList<StoredObject>();
            for (var r : minio.listObjects(args.build())) {
                Item item = r.get();
                results.add(new StoredObject(
                        item.objectName(),
                        item.size(),
                        item.lastModified() != null ? item.lastModified().toInstant() : null,
                        item.etag()));
            }
            return results;
        } catch (Exception e) {
            throw new StorageException("List failed: " + bucket + "/" + prefix, e);
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank() || INVALID_KEY.matcher(key).matches()) {
            throw new StorageException.InvalidKey(key);
        }
    }

    public record PresignedUploadResult(String url, Map<String, String> headers, long expiresInSeconds) {}
    public record PresignedDownloadResult(String url, long expiresInSeconds) {}
    public record StoredObject(String key, long size, java.time.Instant lastModified, String etag) {}
}

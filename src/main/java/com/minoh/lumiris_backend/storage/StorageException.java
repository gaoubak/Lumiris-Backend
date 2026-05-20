package com.minoh.lumiris_backend.storage;

public class StorageException extends RuntimeException {
    public StorageException(String message) { super(message); }
    public StorageException(String message, Throwable cause) { super(message, cause); }

    public static class NotFound extends StorageException {
        public NotFound(String bucket, String key) { super("Object not found: " + bucket + "/" + key); }
    }
    public static class InvalidKey extends StorageException {
        public InvalidKey(String key) { super("Invalid key (path traversal or control chars): " + key); }
    }
    public static class UploadFailed extends StorageException {
        public UploadFailed(String bucket, String key, Throwable cause) { super("Upload failed: " + bucket + "/" + key, cause); }
    }
}

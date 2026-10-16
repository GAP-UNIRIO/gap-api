package com.gap.api.Service;

import com.gap.api.Service.Interface.IStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.exception.SdkClientException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AWSStorageService implements IStorageService {
    private final S3Client s3Client;
    private final AwsCredentialsProvider credentialsProvider;
    private final ObjectMapper objectMapper;
    @Value("${app.storage.s3.bucket:}")
    private String bucket;
    @Value("${app.storage.s3.region:us-east-1}")
    private String region;
    @Value("${app.storage.s3.temporary-prefix:temporary}")
    private String temporaryPrefix;
    @Value("${app.storage.s3.orders-prefix:orders}")
    private String ordersPrefix;
    @Value("${app.storage.s3.upload-key-secret:}")
    private String uploadKeySecret;

    @Override
    public UploadUrlResponse generateUploadUrl(String name, String contentType) {
        validateConfiguration();
        String id = UUID.randomUUID().toString();
        String key = temporaryPrefix + "/" + id + "." + sign(temporaryPrefix + "/", id);
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(600);
        AwsCredentials credentials;
        try { credentials = credentialsProvider.resolveCredentials(); }
        catch (SdkClientException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Storage credentials are unavailable", exception);
        }
        String date = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(java.time.ZoneOffset.UTC).format(now);
        String day = date.substring(0, 8);
        String scope = day + "/" + region + "/s3/aws4_request";
        String credential = credentials.accessKeyId() + "/" + scope;
        Map<String, Object> policy = new LinkedHashMap<>();
        java.util.List<Object> conditions = new java.util.ArrayList<>(java.util.List.of(
                Map.of("bucket", bucket), Map.of("key", key), Map.of("Content-Type", contentType),
                Map.of("x-amz-algorithm", "AWS4-HMAC-SHA256"), Map.of("x-amz-credential", credential),
                Map.of("x-amz-date", date)));
        if (credentials instanceof AwsSessionCredentials session) conditions.add(Map.of("x-amz-security-token", session.sessionToken()));
        policy.put("expiration", expiry.toString());
        policy.put("conditions", conditions);
        try {
            String policyJson = objectMapper.writeValueAsString(policy);
            String encodedPolicy = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));
            String signature = HexFormat.of().formatHex(hmac(signingKey(credentials.secretAccessKey(), day), encodedPolicy));
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("key", key);
            fields.put("Content-Type", contentType);
            fields.put("policy", encodedPolicy);
            fields.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
            fields.put("x-amz-credential", credential);
            fields.put("x-amz-date", date);
            fields.put("x-amz-signature", signature);
            if (credentials instanceof AwsSessionCredentials session) fields.put("x-amz-security-token", session.sessionToken());
            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
            return new UploadUrlResponse(url, key, fields);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to sign storage upload", exception);
        }
    }

    @Override
    public boolean isTemporaryKeyIssued(String key) {
        if (key == null || uploadKeySecret == null || uploadKeySecret.length() < 32) return false;
        String prefix = temporaryPrefix + "/";
        if (!key.startsWith(prefix)) return false;
        String[] parts = key.substring(prefix.length()).split("\\.", -1);
        if (parts.length != 2) return false;
        try { UUID.fromString(parts[0]); } catch (IllegalArgumentException exception) { return false; }
        byte[] expected = sign(prefix, parts[0]).getBytes(StandardCharsets.UTF_8);
        byte[] actual = parts[1].getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    @Override
    public String moveToOrder(String temporaryKey, Long orderId, String name) {
        if (!isTemporaryKeyIssued(temporaryKey)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid temporary file key");
        validateConfiguration();
        String destination = ordersPrefix + "/" + orderId + "/" + UUID.randomUUID() + "/" + safeName(name);
        s3Client.copyObject(CopyObjectRequest.builder().sourceBucket(bucket).sourceKey(temporaryKey)
                .destinationBucket(bucket).destinationKey(destination).build());
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(temporaryKey).build());
        } catch (RuntimeException failure) {
            try { deleteObject(destination); } catch (RuntimeException rollbackFailure) { failure.addSuppressed(rollbackFailure); }
            throw failure;
        }
        return destination;
    }

    @Override public void deleteObject(String key) {
        validateConfiguration();
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    private void validateConfiguration() {
        if (bucket == null || bucket.isBlank() || region == null || region.isBlank()
                || uploadKeySecret == null || uploadKeySecret.length() < 32)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Storage is not configured");
    }
    private String sign(String prefix, String id) {
        return HexFormat.of().formatHex(hmac(uploadKeySecret.getBytes(StandardCharsets.UTF_8), prefix + id));
    }
    private byte[] hmac(byte[] key, String value) {
        try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(key, "HmacSHA256")); return mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }
    private byte[] signingKey(String secret, String date) {
        byte[] dateKey = hmac(("AWS4" + secret).getBytes(StandardCharsets.UTF_8), date);
        byte[] regionKey = hmac(dateKey, region);
        byte[] serviceKey = hmac(regionKey, "s3");
        return hmac(serviceKey, "aws4_request");
    }
    private String safeName(String name) {
        if (name == null || name.isBlank()) return "attachment";
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}

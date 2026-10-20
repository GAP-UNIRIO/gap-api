package com.gap.api.Service;

import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AWSStorageServiceTest {

    private static final String UPLOAD_SECRET = "01234567890123456789012345678901";

    @Test
    void generateUploadUrl_bindsPolicyToExactKeyAndContentType() throws Exception {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        IStorageService.UploadUrlResponse response = service.generateUploadUrl("record.pdf", "application/pdf");

        assertTrue(response.uploadUrl().startsWith("https://uploads-test.s3.us-east-1.amazonaws.com/"));
        assertEquals(response.fileKey(), response.fields().get("key"));
        assertEquals("application/pdf", response.fields().get("Content-Type"));
        String policy = new String(Base64.getDecoder().decode(response.fields().get("policy")), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(policy.contains(response.fileKey()));
        assertTrue(policy.contains("application/pdf"));
        assertTrue(response.fields().containsKey("x-amz-signature"));
        Instant expiration = Instant.parse(new ObjectMapper().readTree(policy).get("expiration").asString());
        assertTrue(expiration.isAfter(Instant.now()));
        assertTrue(expiration.isBefore(Instant.now().plus(Duration.ofMinutes(11))));
        assertTrue(service.isTemporaryKeyIssued(response.fileKey()));
    }

    @Test
    void generateUploadUrl_includesSessionToken() {
        String token = "session-token-for-test";
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsSessionCredentials.create("access-key", "secret-key-for-test-only", token));

        var response = service.generateUploadUrl("record.pdf", "application/pdf");

        assertEquals(token, response.fields().get("x-amz-security-token"));
    }

    @Test
    void generateUploadUrl_failsWhenConfigurationIsMissing() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        ReflectionTestUtils.setField(service, "bucket", "");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl("record.pdf", "application/pdf"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void isTemporaryKeyIssued_rejectsMalformedAndTamperedKeys() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String validKey = service.generateUploadUrl("record.pdf", "application/pdf").fileKey();

        assertTrue(service.isTemporaryKeyIssued(validKey));
        assertFalse(service.isTemporaryKeyIssued(validKey + "tampered"));
        assertFalse(service.isTemporaryKeyIssued("temporary/not-a-uuid.signature"));
        assertFalse(service.isTemporaryKeyIssued("orders/23/proof.pdf"));
        assertFalse(service.isTemporaryKeyIssued(null));
    }

    @Test
    void moveToOrder_copiesToFinalKeyAndDeletesTemporaryKey() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl("record.pdf", "application/pdf").fileKey();

        String destinationKey = service.moveToOrder(temporaryKey, 23L, "record.pdf");

        assertTrue(destinationKey.matches("orders/23/[^/]+/record\\.pdf"));
        verify(s3Client).copyObject(org.mockito.ArgumentMatchers.<CopyObjectRequest>argThat(request ->
                request.sourceBucket().equals("uploads-test")
                        && request.sourceKey().equals(temporaryKey)
                        && request.destinationBucket().equals("uploads-test")
                        && request.destinationKey().equals(destinationKey)));
        verify(s3Client).deleteObject(org.mockito.ArgumentMatchers.<DeleteObjectRequest>argThat(request ->
                request.bucket().equals("uploads-test") && request.key().equals(temporaryKey)));
    }

    @Test
    void moveToOrder_rejectsInvalidKeyBeforeS3Calls() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.moveToOrder("temporary/invalid", 23L, "record.pdf"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    void moveToOrder_doesNotDeleteSourceWhenCopyFails() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl("record.pdf", "application/pdf").fileKey();
        RuntimeException copyFailure = new RuntimeException("copy failed");
        when(s3Client.copyObject(any(CopyObjectRequest.class))).thenThrow(copyFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.moveToOrder(temporaryKey, 23L, "record.pdf"));

        assertSame(copyFailure, thrown);
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void moveToOrder_deletesDestinationWhenTemporaryCleanupFails() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl("record.pdf", "application/pdf").fileKey();
        RuntimeException cleanupFailure = new RuntimeException("temporary delete failed");
        List<DeleteObjectRequest> deleteRequests = new ArrayList<>();
        doAnswer(invocation -> {
            DeleteObjectRequest request = invocation.getArgument(0);
            deleteRequests.add(request);
            if (temporaryKey.equals(request.key())) throw cleanupFailure;
            return DeleteObjectResponse.builder().build();
        }).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.moveToOrder(temporaryKey, 23L, "record.pdf"));

        assertSame(cleanupFailure, thrown);
        assertEquals(2, deleteRequests.size());
        assertEquals(temporaryKey, deleteRequests.get(0).key());
        assertTrue(deleteRequests.get(1).key().matches("orders/23/[^/]+/record\\.pdf"));
    }

    private AWSStorageService configuredService(S3Client s3Client, AwsCredentialsProvider credentialsProvider) {
        AWSStorageService service = new AWSStorageService(s3Client, credentialsProvider, new ObjectMapper());
        ReflectionTestUtils.setField(service, "bucket", "uploads-test");
        ReflectionTestUtils.setField(service, "region", "us-east-1");
        ReflectionTestUtils.setField(service, "temporaryPrefix", "temporary");
        ReflectionTestUtils.setField(service, "ordersPrefix", "orders");
        ReflectionTestUtils.setField(service, "uploadKeySecret", UPLOAD_SECRET);
        return service;
    }
}

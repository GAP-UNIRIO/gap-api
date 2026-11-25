package com.gap.api.Service;

import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
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

        IStorageService.UploadUrlResponse response = service.generateUploadUrl(7L, "record.pdf", "application/pdf");

        assertTrue(response.fileKey().startsWith("temporary/7/"));
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
    @org.junit.jupiter.api.DisplayName("@spec:AC-178 restringe o POST assinado a arquivos de até 5 MiB")
    void generateUploadUrl_policyLimitsUploadToFiveMiB() throws Exception {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        var response = service.generateUploadUrl(7L, "record.pdf", "application/pdf");
        String policy = new String(Base64.getDecoder().decode(response.fields().get("policy")),
                java.nio.charset.StandardCharsets.UTF_8);

        assertTrue(policy.contains("content-length-range"));
        assertTrue(policy.contains("5242880"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@spec:AC-178 rejeita MIME type não permitido")
    void generateUploadUrl_rejectsUnsupportedContentType() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl(7L, "script.exe", "application/x-msdownload"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void generateUploadUrl_rejectsNullContentType() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl(7L, "record.pdf", null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void generateUploadUrl_rejectsMissingOrInvalidOwner() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        for (Long invalidOwner : new Long[]{null, 0L, -1L}) {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> service.generateUploadUrl(invalidOwner, "record.pdf", "application/pdf"));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        }
    }

    @Test
    void generateUploadUrl_returns503WhenCredentialsCannotBeResolved() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> { throw SdkClientException.create("credentials unavailable"); });

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl(7L, "record.pdf", "application/pdf"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("Storage credentials are unavailable", exception.getReason());
    }

    @Test
    void generateUploadUrl_returns503WhenPolicySerializationFails() throws Exception {
        S3Client s3Client = mock(S3Client.class);
        AwsCredentialsProvider credentials = () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only");
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any())).thenThrow(new IllegalStateException("serialization failed"));
        AWSStorageService service = new AWSStorageService(s3Client, credentials, mapper);
        setConfiguration(service);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl(7L, "record.pdf", "application/pdf"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("Unable to sign storage upload", exception.getReason());
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@spec:AC-178 aceita PDF, JPEG, PNG e DOCX")
    void generateUploadUrl_acceptsAllowedContentTypes() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        assertDoesNotThrow(() -> service.generateUploadUrl(7L, "record.pdf", "application/pdf"));
        assertDoesNotThrow(() -> service.generateUploadUrl(7L, "record.jpg", "image/jpeg"));
        assertDoesNotThrow(() -> service.generateUploadUrl(7L, "record.png", "image/png"));
        assertDoesNotThrow(() -> service.generateUploadUrl(7L, "record.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    @Test
    @org.junit.jupiter.api.DisplayName("@spec:AC-178 consulta o tamanho do objeto temporário no S3")
    void getTemporaryObjectSize_returnsS3ContentLength() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String key = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
        when(s3Client.headObject(any(software.amazon.awssdk.services.s3.model.HeadObjectRequest.class)))
                .thenReturn(software.amazon.awssdk.services.s3.model.HeadObjectResponse.builder().contentLength(123L).build());

        assertEquals(123L, service.getTemporaryObjectSize(key));
    }

    @Test
    void getTemporaryObjectSize_mapsMissingObjectToBadRequest() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String key = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(404).message("missing").build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getTemporaryObjectSize(key));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Temporary file was not found", exception.getReason());
    }

    @Test
    void getTemporaryObjectSize_mapsStorageFailureToServiceUnavailable() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String key = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(S3Exception.builder().statusCode(503).message("unavailable").build());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getTemporaryObjectSize(key));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("Unable to verify temporary file size", exception.getReason());
    }

    @Test
    void getTemporaryObjectSize_rejectsInvalidKeyWithoutCallingS3() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getTemporaryObjectSize("temporary/invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    void getTemporaryObjectSize_returns503WhenConfigurationIsIncomplete() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String key = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
        ReflectionTestUtils.setField(service, "region", " ");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getTemporaryObjectSize(key));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void deleteObject_returns503ForEachMissingConfigurationValue() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));

        for (String[] invalidConfiguration : new String[][]{
                {"bucket", null}, {"region", null}, {"uploadKeySecret", null}, {"uploadKeySecret", "short"}
        }) {
            ReflectionTestUtils.setField(service, invalidConfiguration[0], invalidConfiguration[1]);
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> service.deleteObject("orders/23/file.pdf"));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
            setConfiguration(service);
        }
    }

    @Test
    void generateUploadUrl_includesSessionToken() {
        String token = "session-token-for-test";
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsSessionCredentials.create("access-key", "secret-key-for-test-only", token));

        var response = service.generateUploadUrl(7L, "record.pdf", "application/pdf");

        assertEquals(token, response.fields().get("x-amz-security-token"));
    }

    @Test
    void generateUploadUrl_failsWhenConfigurationIsMissing() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        ReflectionTestUtils.setField(service, "bucket", "");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.generateUploadUrl(7L, "record.pdf", "application/pdf"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void isTemporaryKeyIssued_rejectsMalformedAndTamperedKeys() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String validKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();

        assertTrue(service.isTemporaryKeyIssued(validKey));
        assertTrue(service.isTemporaryKeyIssuedForUser(validKey, 7L));
        assertFalse(service.isTemporaryKeyIssuedForUser(validKey, 8L));
        assertFalse(service.isTemporaryKeyIssuedForUser("temporary/invalid", 7L));
        assertFalse(service.isTemporaryKeyIssued(validKey + "tampered"));
        assertFalse(service.isTemporaryKeyIssued("temporary/not-a-uuid.signature"));
        assertFalse(service.isTemporaryKeyIssued("temporary/not-a-number/id.signature"));
        assertFalse(service.isTemporaryKeyIssued("temporary/0/id.signature"));
        assertFalse(service.isTemporaryKeyIssued("temporary/7/id.signature"));
        assertFalse(service.isTemporaryKeyIssuedForUser(validKey, null));
        assertFalse(service.isTemporaryKeyIssued("temporary/550e8400-e29b-41d4-a716-446655440000.legacy-signature"));
        assertFalse(service.isTemporaryKeyIssued("orders/23/proof.pdf"));
        assertFalse(service.isTemporaryKeyIssued(null));
    }

    @Test
    void isTemporaryKeyIssued_rejectsKeysWhenSecretIsMissingOrTooShort() {
        AWSStorageService service = configuredService(mock(S3Client.class),
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String validKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();

        ReflectionTestUtils.setField(service, "uploadKeySecret", null);
        assertFalse(service.isTemporaryKeyIssued(validKey));
        ReflectionTestUtils.setField(service, "uploadKeySecret", "short");
        assertFalse(service.isTemporaryKeyIssued(validKey));
    }

    @Test
    void moveToOrder_copiesToFinalKeyAndDeletesTemporaryKey() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();

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
        String temporaryKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
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
        String temporaryKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
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

    @Test
    void moveToOrder_addsRollbackFailureAsSuppressedWhenCleanupAlsoFails() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();
        RuntimeException sourceCleanupFailure = new RuntimeException("source cleanup failed");
        RuntimeException rollbackFailure = new RuntimeException("rollback cleanup failed");
        List<DeleteObjectRequest> deleteRequests = new ArrayList<>();
        doAnswer(invocation -> {
            DeleteObjectRequest request = invocation.getArgument(0);
            deleteRequests.add(request);
            if (temporaryKey.equals(request.key())) throw sourceCleanupFailure;
            throw rollbackFailure;
        }).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> service.moveToOrder(temporaryKey, 23L, "record.pdf"));

        assertSame(sourceCleanupFailure, thrown);
        assertArrayEquals(new Throwable[]{rollbackFailure}, thrown.getSuppressed());
        assertEquals(2, deleteRequests.size());
    }

    @Test
    void moveToOrder_usesFallbackNameAndSanitizesUnsafeCharacters() {
        S3Client s3Client = mock(S3Client.class);
        AWSStorageService service = configuredService(s3Client,
                () -> AwsBasicCredentials.create("access-key", "secret-key-for-test-only"));
        String temporaryKey = service.generateUploadUrl(7L, "record.pdf", "application/pdf").fileKey();

        String fallbackNameDestination = service.moveToOrder(temporaryKey, 23L, null);
        String blankNameDestination = service.moveToOrder(temporaryKey, 23L, " ");
        String safeNameDestination = service.moveToOrder(temporaryKey, 23L, "my file?.pdf");

        assertTrue(fallbackNameDestination.endsWith("/attachment"));
        assertTrue(blankNameDestination.endsWith("/attachment"));
        assertTrue(safeNameDestination.endsWith("/my_file_.pdf"));
    }

    private AWSStorageService configuredService(S3Client s3Client, AwsCredentialsProvider credentialsProvider) {
        AWSStorageService service = new AWSStorageService(s3Client, credentialsProvider, new ObjectMapper());
        setConfiguration(service);
        return service;
    }

    private void setConfiguration(AWSStorageService service) {
        ReflectionTestUtils.setField(service, "bucket", "uploads-test");
        ReflectionTestUtils.setField(service, "region", "us-east-1");
        ReflectionTestUtils.setField(service, "temporaryPrefix", "temporary");
        ReflectionTestUtils.setField(service, "ordersPrefix", "orders");
        ReflectionTestUtils.setField(service, "uploadKeySecret", UPLOAD_SECRET);
    }
}

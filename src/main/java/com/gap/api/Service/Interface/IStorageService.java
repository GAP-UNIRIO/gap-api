package com.gap.api.Service.Interface;

import java.util.Map;

public interface IStorageService {
    long MAX_ANNEX_SIZE_BYTES = 5L * 1024 * 1024;
    record UploadUrlResponse(String uploadUrl, String fileKey, Map<String, String> fields) { }
    UploadUrlResponse generateUploadUrl(Long ownerUserId, String name, String contentType);
    boolean isTemporaryKeyIssued(String key);
    boolean isTemporaryKeyIssuedForUser(String key, Long ownerUserId);
    long getTemporaryObjectSize(String key);
    String moveToOrder(String temporaryKey, Long orderId, String name);
    void deleteObject(String key);
}

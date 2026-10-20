package com.gap.api.Service.Interface;

import java.util.Map;

public interface IStorageService {
    record UploadUrlResponse(String uploadUrl, String fileKey, Map<String, String> fields) { }
    UploadUrlResponse generateUploadUrl(String name, String contentType);
    boolean isTemporaryKeyIssued(String key);
    String moveToOrder(String temporaryKey, Long orderId, String name);
    void deleteObject(String key);
}
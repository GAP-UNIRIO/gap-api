package com.gap.api.Model.DTO;

import java.util.Map;

public record AnnexUploadResponse(String uploadUrl, String fileKey, Map<String, String> fields) { }
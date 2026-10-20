package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.AnnexUploadRequest;
import com.gap.api.Model.DTO.AnnexUploadResponse;
import com.gap.api.Model.DTO.OrderRequest;
import com.gap.api.Model.DTO.OrderResponse;

public interface IOrderService {
    OrderResponse create(OrderRequest request);
    AnnexUploadResponse generateAnnexUploadUrl(AnnexUploadRequest request);
}
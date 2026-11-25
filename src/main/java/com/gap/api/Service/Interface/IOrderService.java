package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.AnnexUploadRequest;
import com.gap.api.Model.DTO.AnnexUploadResponse;
import com.gap.api.Model.DTO.OrderRequest;
import com.gap.api.Model.DTO.OrderResponse;
import com.gap.api.Model.DTO.ApprovalDecisionRequest;

public interface IOrderService {
    OrderResponse create(OrderRequest request, String authenticatedEmail);
    AnnexUploadResponse generateAnnexUploadUrl(AnnexUploadRequest request, String authenticatedEmail);
    OrderResponse cancel(Long orderId, String authenticatedEmail);
    OrderResponse decideStage(Long orderId, Long stageId, ApprovalDecisionRequest request, String authenticatedEmail);
    OrderResponse complete(Long orderId, String authenticatedEmail);
}

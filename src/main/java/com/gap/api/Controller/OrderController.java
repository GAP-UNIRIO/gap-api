package com.gap.api.Controller;

import com.gap.api.Model.DTO.*;
import com.gap.api.Service.Interface.IOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final IOrderService orderService;
    public OrderController(IOrderService orderService) { this.orderService = orderService; }
    @PostMapping("/annex-upload")
    public ResponseEntity<BaseResponse<AnnexUploadResponse>> generateUpload(@Valid @RequestBody AnnexUploadRequest request) {
        return ResponseEntity.ok(BaseResponse.success("Upload authorization created.", orderService.generateAnnexUploadUrl(request)));
    }
    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> create(
            @AuthenticationPrincipal OidcUser authenticatedUser,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("Order created.",
                        orderService.create(request, authenticatedUser.getEmail())));
    }
}

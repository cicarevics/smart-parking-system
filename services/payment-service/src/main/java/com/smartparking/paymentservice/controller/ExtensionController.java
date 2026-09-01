package com.smartparking.paymentservice.controller;

import com.smartparking.paymentservice.dto.ExtensionCreateRequest;
import com.smartparking.paymentservice.dto.PaymentResponse;
import com.smartparking.paymentservice.model.Payment;
import com.smartparking.paymentservice.model.PaymentStatus;
import com.smartparking.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// External path is /payments/extensions (gateway's StripPrefix=1 on
// /payments/** leaves /extensions, which this maps to) -- a sub-resource
// of payments, not a new gateway route.
@RestController
@RequestMapping("/extensions")
public class ExtensionController {

    private final PaymentService paymentService;

    public ExtensionController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody ExtensionCreateRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            Authentication authentication
    ) {
        Payment payment = paymentService.payForExtension(userId(authentication), authorizationHeader, request);
        HttpStatus status = payment.getStatus() == PaymentStatus.SUCCEEDED
                ? HttpStatus.CREATED
                : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(new PaymentResponse(payment));
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}

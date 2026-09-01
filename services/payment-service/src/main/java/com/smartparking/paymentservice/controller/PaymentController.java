package com.smartparking.paymentservice.controller;

import com.smartparking.paymentservice.dto.PaymentCreateRequest;
import com.smartparking.paymentservice.dto.PaymentResponse;
import com.smartparking.paymentservice.model.Payment;
import com.smartparking.paymentservice.model.PaymentStatus;
import com.smartparking.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// No class-level @RequestMapping prefix -- same convention as the other
// services; the gateway's StripPrefix=1 already removes /payments before
// forwarding.
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/")
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody PaymentCreateRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            Authentication authentication
    ) {
        Payment payment = paymentService.pay(userId(authentication), authorizationHeader, request);
        HttpStatus status = payment.getStatus() == PaymentStatus.SUCCEEDED
                ? HttpStatus.CREATED
                : HttpStatus.PAYMENT_REQUIRED;
        return ResponseEntity.status(status).body(new PaymentResponse(payment));
    }

    @GetMapping("/")
    public List<PaymentResponse> listMine(Authentication authentication) {
        return paymentService.listMine(userId(authentication)).stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}

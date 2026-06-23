package com.smartbank.controller;

import com.smartbank.dto.request.AddMoneyRequest;
import com.smartbank.dto.request.TransferRequest;
import com.smartbank.dto.response.ApiResponse;
import com.smartbank.dto.response.TransactionResponse;
import com.smartbank.dto.response.WalletResponse;
import com.smartbank.service.impl.PdfStatementService;
import com.smartbank.service.impl.WalletServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@SuppressWarnings("null")
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletServiceImpl walletService;
    private final PdfStatementService pdfService;

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(Principal principal) {
        return ResponseEntity.ok(walletService.getWalletByUsername(principal.getName()));
    }

    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<TransactionResponse>> addMoney(
            Principal principal, @Valid @RequestBody AddMoneyRequest request) {
        return ResponseEntity.ok(walletService.addMoney(principal.getName(), request));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            Principal principal, @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(walletService.transferMoney(principal.getName(), request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(walletService.getTransactionHistory(principal.getName(), pageable));
    }

    @GetMapping("/lookup/{accountNumber}")
    public ResponseEntity<ApiResponse<Map<String, String>>> lookupAccount(
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(walletService.lookupAccount(accountNumber));
    }

    @PutMapping("/set-pin")
    public ResponseEntity<ApiResponse<String>> setPin(
            Principal principal, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(walletService.setPin(principal.getName(), body.get("pin")));
    }

    @GetMapping("/statement/pdf")
    public ResponseEntity<byte[]> downloadStatement(
            Principal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        byte[] pdf = pdfService.generateStatement(principal.getName(), from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

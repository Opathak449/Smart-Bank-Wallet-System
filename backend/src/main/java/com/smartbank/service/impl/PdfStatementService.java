package com.smartbank.service.impl;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.smartbank.entity.Transaction;
import com.smartbank.entity.User;
import com.smartbank.entity.Wallet;
import com.smartbank.exception.ResourceNotFoundException;
import com.smartbank.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfStatementService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private static final DeviceRgb PRIMARY_BLUE = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(245, 245, 245);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public byte[] generateStatement(String username, LocalDateTime from, LocalDateTime to) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        List<Transaction> transactions = transactionRepository
                .findByWalletIdAndDateRange(wallet.getId(), from, to);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 40, 40, 40);

            // Header
            Paragraph header = new Paragraph("🏦 SmartBank Wallet")
                    .setFontSize(24).setBold().setFontColor(PRIMARY_BLUE)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(header);

            doc.add(new Paragraph("BANK STATEMENT")
                    .setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2f)));
            doc.add(new Paragraph("\n"));

            // Customer Info Table
            float[] colWidths = {200f, 300f};
            Table infoTable = new Table(colWidths);
            infoTable.addCell(infoCell("Customer Name:", true));
            infoTable.addCell(infoCell(user.getFullName(), false));
            infoTable.addCell(infoCell("Account Number:", true));
            infoTable.addCell(infoCell(wallet.getAccountNumber(), false));
            infoTable.addCell(infoCell("Wallet ID:", true));
            infoTable.addCell(infoCell(wallet.getWalletId(), false));
            infoTable.addCell(infoCell("Statement Period:", true));
            infoTable.addCell(infoCell(from.format(DTF) + " to " + to.format(DTF), false));
            infoTable.addCell(infoCell("Generated On:", true));
            infoTable.addCell(infoCell(LocalDateTime.now().format(DTF), false));
            doc.add(infoTable);
            doc.add(new Paragraph("\n"));

            // Balance Summary
            BigDecimal totalCredit = transactions.stream()
                    .filter(t -> t.getReceiverWallet() != null && t.getReceiverWallet().getId().equals(wallet.getId()))
                    .filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
                    .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalDebit = transactions.stream()
                    .filter(t -> t.getSenderWallet() != null && t.getSenderWallet().getId().equals(wallet.getId()))
                    .filter(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS)
                    .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            Table balanceTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
            balanceTable.addCell(balanceCell("Current Balance\n₹" + wallet.getBalance(), PRIMARY_BLUE));
            balanceTable.addCell(balanceCell("Total Credit\n₹" + totalCredit, new DeviceRgb(46, 125, 50)));
            balanceTable.addCell(balanceCell("Total Debit\n₹" + totalDebit, new DeviceRgb(198, 40, 40)));
            doc.add(balanceTable);
            doc.add(new Paragraph("\n"));

            // Transactions Table
            doc.add(new Paragraph("Transaction Details").setFontSize(14).setBold().setFontColor(PRIMARY_BLUE));
            float[] txnWidths = {120f, 80f, 80f, 80f, 80f, 80f};
            Table txnTable = new Table(txnWidths).useAllAvailableWidth();

            String[] headers = {"Date", "Type", "Amount", "Status", "Balance After", "Description"};
            for (String h : headers) {
                txnTable.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(PRIMARY_BLUE).setPadding(8));
            }

            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                boolean isEven = i % 2 == 0;
                txnTable.addCell(txnCell(t.getCreatedAt() != null ? t.getCreatedAt().format(DTF) : "N/A", isEven));
                txnTable.addCell(txnCell(t.getTransactionType().name(), isEven));
                txnTable.addCell(txnCell("₹" + t.getAmount(), isEven));
                txnTable.addCell(txnCell(t.getStatus().name(), isEven));
                txnTable.addCell(txnCell(t.getBalanceAfter() != null ? "₹" + t.getBalanceAfter() : "N/A", isEven));
                txnTable.addCell(txnCell(t.getDescription() != null ? t.getDescription() : "-", isEven));
            }

            if (transactions.isEmpty()) {
                Cell emptyCell = new Cell(1, 6)
                        .add(new Paragraph("No transactions found for the selected period").setTextAlignment(TextAlignment.CENTER))
                        .setPadding(20);
                txnTable.addCell(emptyCell);
            }
            doc.add(txnTable);

            // Footer
            doc.add(new Paragraph("\n\nThis is a system-generated statement. No signature required.")
                    .setFontSize(10).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

            doc.close();
        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate PDF statement");
        }
        return baos.toByteArray();
    }

    private Cell infoCell(String text, boolean isLabel) {
        Cell cell = new Cell().add(new Paragraph(text)).setPadding(6).setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f));
        if (isLabel) cell.setBold().setFontColor(PRIMARY_BLUE);
        return cell;
    }

    private Cell balanceCell(String text, DeviceRgb color) {
        return new Cell().add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(color).setPadding(15);
    }

    private Cell txnCell(String text, boolean isEven) {
        Cell cell = new Cell().add(new Paragraph(text)).setPadding(6);
        if (isEven) cell.setBackgroundColor(LIGHT_GRAY);
        return cell;
    }
}

package com.billMate.billing.infrastructure.pdf;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
@Slf4j
public class PdfGeneratorAdapter implements PdfGeneratorPort {

    @Override
    public byte[] generate(Invoice invoice, Client client) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Paragraph title = new Paragraph("FACTURA #" + invoice.getId(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Cliente: ", labelFont));
            document.add(new Paragraph(client.getName(), normalFont));
            document.add(new Paragraph("Email: " + client.getEmail(), normalFont));
            document.add(new Paragraph("Fecha: " + invoice.getDate(), normalFont));
            document.add(new Paragraph("Estado: " + invoice.getStatus(), normalFont));
            document.add(new Paragraph("Descripción: " +
                    (invoice.getDescription() != null ? invoice.getDescription() : "-"), normalFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{5, 2, 2, 2});
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableHeader(table, "Descripción", "Cantidad", "Precio Unitario", "Subtotal");

            for (InvoiceLineItem line : invoice.getLines()) {
                table.addCell(createCell(line.getDescription(), normalFont));
                table.addCell(createCell(line.getQuantity().toPlainString(), normalFont));
                table.addCell(createCell(line.getUnitPrice().toPlainString() + " €", normalFont));
                table.addCell(createCell(line.getTotal().toPlainString() + " €", normalFont));
            }

            document.add(table);

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph total = new Paragraph("Total: " + invoice.getTotal().toPlainString() + " €", totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(20f);
            document.add(total);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de factura", e);
            throw new RuntimeException("No se pudo generar el PDF");
        }
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(63, 81, 181));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }
}

package com.billMate.billing.infrastructure.pdf;

import com.billMate.billing.domain.client.model.Client;
import com.billMate.billing.domain.invoice.model.Invoice;
import com.billMate.billing.domain.invoice.model.InvoiceLineItem;
import com.billMate.billing.domain.invoice.port.out.PdfGeneratorPort;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Primary
@Slf4j
public class StyledPdfGeneratorAdapter implements PdfGeneratorPort {

    private static final BaseColor BRAND_PRIMARY = new BaseColor(23, 63, 95);
    private static final BaseColor BRAND_SECONDARY = new BaseColor(51, 122, 183);
    private static final BaseColor SURFACE = new BaseColor(244, 247, 250);
    private static final BaseColor BORDER = new BaseColor(220, 227, 234);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] generate(Invoice invoice, Client client) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 42, 42);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.WHITE);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new BaseColor(225, 235, 242));
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_PRIMARY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND_PRIMARY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(55, 65, 81));
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new BaseColor(41, 51, 63));
            Font totalLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BRAND_PRIMARY);
            Font totalValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, BRAND_PRIMARY);

            document.add(createHeader(invoice, client, titleFont, subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable summary = new PdfPTable(2);
            summary.setWidthPercentage(100);
            summary.setWidths(new float[]{1.2f, 1.8f});
            summary.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            summary.addCell(createInfoBox(
                    "Cliente",
                    List.of(
                            client.getName(),
                            safeValue(client.getEmail()),
                            safeValue(client.getAddress())
                    ),
                    sectionFont,
                    valueFont
            ));
            summary.addCell(createInfoBox(
                    "Factura",
                    List.of(
                            "Fecha: " + formatDate(invoice),
                            "Estado: " + invoice.getStatus(),
                            "Concepto: " + safeValue(invoice.getDescription())
                    ),
                    sectionFont,
                    valueFont
            ));
            document.add(summary);
            document.add(new Paragraph(" "));

            document.add(createServicesBlock(invoice.getLines(), sectionFont, valueFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{5.5f, 1.4f, 2.1f, 2.2f});
            table.setSpacingBefore(6f);
            table.setSpacingAfter(10f);

            addTableHeader(table, tableHeaderFont, "Servicio", "Cantidad", "Precio", "Importe");

            boolean alternateRow = false;
            for (InvoiceLineItem line : invoice.getLines()) {
                BaseColor rowBackground = alternateRow ? SURFACE : BaseColor.WHITE;
                table.addCell(createBodyCell(resolveServiceName(line), tableCellFont, Element.ALIGN_LEFT, rowBackground));
                table.addCell(createBodyCell(formatNumber(line.getQuantity()), tableCellFont, Element.ALIGN_CENTER, rowBackground));
                table.addCell(createBodyCell(formatCurrency(line.getUnitPrice()), tableCellFont, Element.ALIGN_RIGHT, rowBackground));
                table.addCell(createBodyCell(formatCurrency(line.getTotal()), tableCellFont, Element.ALIGN_RIGHT, rowBackground));
                alternateRow = !alternateRow;
            }

            document.add(table);
            document.add(createTotalsBlock(invoice, labelFont, totalLabelFont, totalValueFont));

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating styled invoice PDF", kv("invoiceId", invoice.getId()), e);
            throw new RuntimeException("No se pudo generar el PDF");
        }
    }

    private PdfPTable createHeader(Invoice invoice, Client client, Font titleFont, Font subtitleFont) throws DocumentException {
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{2.4f, 1.2f});

        PdfPCell brandCell = new PdfPCell();
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setBackgroundColor(BRAND_PRIMARY);
        brandCell.setPadding(18f);
        brandCell.addElement(new Paragraph("BillMate", titleFont));
        brandCell.addElement(new Paragraph("Factura profesional para " + client.getName(), subtitleFont));
        header.addCell(brandCell);

        PdfPCell invoiceCell = new PdfPCell();
        invoiceCell.setBorder(Rectangle.NO_BORDER);
        invoiceCell.setBackgroundColor(BRAND_SECONDARY);
        invoiceCell.setPadding(18f);
        Paragraph invoiceNumber = new Paragraph("FACTURA #" + invoice.getId(), titleFont);
        invoiceNumber.setAlignment(Element.ALIGN_RIGHT);
        invoiceCell.addElement(invoiceNumber);
        Paragraph createdAt = new Paragraph("Emitida el " + formatDate(invoice), subtitleFont);
        createdAt.setAlignment(Element.ALIGN_RIGHT);
        invoiceCell.addElement(createdAt);
        header.addCell(invoiceCell);

        return header;
    }

    private PdfPCell createInfoBox(String title, List<String> lines, Font titleFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(14f);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(SURFACE);

        Paragraph sectionTitle = new Paragraph(title, titleFont);
        sectionTitle.setSpacingAfter(6f);
        cell.addElement(sectionTitle);

        for (String line : lines) {
            Paragraph paragraph = new Paragraph(line, valueFont);
            paragraph.setSpacingAfter(3f);
            cell.addElement(paragraph);
        }

        return cell;
    }

    private PdfPTable createServicesBlock(List<InvoiceLineItem> lines, Font titleFont, Font valueFont) {
        PdfPTable block = new PdfPTable(1);
        block.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(14f);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(BaseColor.WHITE);

        Paragraph title = new Paragraph("Servicios incluidos", titleFont);
        title.setSpacingAfter(6f);
        cell.addElement(title);

        String servicesSummary = lines.stream()
                .map(this::resolveServiceName)
                .distinct()
                .reduce((left, right) -> left + " · " + right)
                .orElse("Sin detalle de servicios");

        cell.addElement(new Paragraph(servicesSummary, valueFont));
        block.addCell(cell);
        return block;
    }

    private PdfPTable createTotalsBlock(Invoice invoice, Font labelFont, Font totalLabelFont, Font totalValueFont) throws DocumentException {
        BigDecimal subtotal = invoice.getLines().stream()
                .map(InvoiceLineItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = invoice.getTotal().subtract(subtotal);

        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(42);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setWidths(new float[]{1.2f, 1f});

        totals.addCell(createSummaryCell("Base imponible", labelFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        totals.addCell(createSummaryCell(formatCurrency(subtotal), labelFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
        totals.addCell(createSummaryCell("IVA (" + formatNumber(invoice.getTaxPercentage()) + "%)", labelFont, Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        totals.addCell(createSummaryCell(formatCurrency(taxAmount), labelFont, Element.ALIGN_RIGHT, Rectangle.NO_BORDER));
        totals.addCell(createSummaryCell("Total", totalLabelFont, Element.ALIGN_LEFT, Rectangle.TOP));
        totals.addCell(createSummaryCell(formatCurrency(invoice.getTotal()), totalValueFont, Element.ALIGN_RIGHT, Rectangle.TOP));
        return totals;
    }

    private void addTableHeader(PdfPTable table, Font headerFont, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BRAND_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(9);
            cell.setBorderColor(BRAND_PRIMARY);
            table.addCell(cell);
        }
    }

    private PdfPCell createBodyCell(String content, Font font, int alignment, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(8);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private PdfPCell createSummaryCell(String content, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(8f);
        cell.setBorder(border);
        cell.setBorderColor(BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private String resolveServiceName(InvoiceLineItem line) {
        String description = line.getDescription();
        if (description == null || description.isBlank()) {
            return "Servicio sin descripcion";
        }
        return description;
    }

    private String formatDate(Invoice invoice) {
        return invoice.getDate() != null ? invoice.getDate().format(DATE_FORMAT) : "-";
    }

    private String formatCurrency(BigDecimal amount) {
        return formatNumber(amount) + " EUR";
    }

    private String formatNumber(BigDecimal amount) {
        return amount != null ? amount.stripTrailingZeros().toPlainString() : "-";
    }

    private String safeValue(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }
}

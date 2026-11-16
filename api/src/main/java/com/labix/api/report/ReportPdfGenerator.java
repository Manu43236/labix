package com.labix.api.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

@Component
public class ReportPdfGenerator {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FLAG_BOLD = new Font(Font.HELVETICA, 9, Font.BOLD);

    public byte[] generate(JsonNode snapshot) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            addHeader(doc, snapshot);
            addPatientBlock(doc, snapshot);
            addResultsTables(doc, snapshot);
            addFooter(doc, snapshot);

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private void addHeader(Document doc, JsonNode s) throws DocumentException {
        Paragraph title = new Paragraph("LABORATORY REPORT", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        JsonNode tenant = s.path("tenant");
        Paragraph labName = new Paragraph(tenant.path("name").asText("Lab"), HEADER_FONT);
        labName.setAlignment(Element.ALIGN_CENTER);
        doc.add(labName);

        if (!tenant.path("address").isMissingNode()) {
            Paragraph addr = new Paragraph(tenant.path("address").asText(), SMALL_FONT);
            addr.setAlignment(Element.ALIGN_CENTER);
            doc.add(addr);
        }

        LineSeparator line = new LineSeparator();
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private void addPatientBlock(Document doc, JsonNode s) throws DocumentException {
        JsonNode patient = s.path("patient");
        JsonNode visit = s.path("visit");
        JsonNode doctor = s.path("doctor");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);

        addInfoCell(table, "Patient", patient.path("name").asText());
        addInfoCell(table, "Patient No", String.valueOf(patient.path("patientNo").asInt()));
        addInfoCell(table, "Age / Gender",
                patient.path("ageText").asText() + " / " + patient.path("gender").asText());
        addInfoCell(table, "Visit No", String.valueOf(visit.path("visitNo").asInt()));

        addInfoCell(table, "Bill No", String.valueOf(visit.path("billNo").asInt()));
        addInfoCell(table, "Visit Date", visit.path("visitDate").asText());
        addInfoCell(table, "Ref Doctor",
                doctor.isMissingNode() ? "-" : doctor.path("name").asText());
        addInfoCell(table, "Visit Type", visit.path("visitType").asText());

        doc.add(table);
        LineSeparator line = new LineSeparator();
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private void addResultsTables(Document doc, JsonNode s) throws DocumentException {
        JsonNode tests = s.path("tests");
        for (JsonNode test : tests) {
            Paragraph testHeading = new Paragraph(
                    test.path("testName").asText() + " (" + test.path("category").asText("") + ")",
                    new Font(Font.HELVETICA, 10, Font.BOLD));
            testHeading.setSpacingBefore(6);
            doc.add(testHeading);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{30, 15, 10, 25, 10});
            table.setSpacingAfter(4);

            addTableHeader(table, "Parameter", "Result", "Unit", "Reference Range", "Flag");

            for (JsonNode param : test.path("parameters")) {
                String flag = param.path("flag").asText("");
                boolean abnormal = "HIGH".equals(flag) || "LOW".equals(flag) || "CRITICAL".equals(flag);
                Font valueFont = abnormal ? FLAG_BOLD : NORMAL_FONT;

                addCell(table, param.path("name").asText(), NORMAL_FONT);
                PdfPCell valueCell = new PdfPCell(new Phrase(formatValue(param), valueFont));
                valueCell.setBorder(Rectangle.BOX);
                valueCell.setPadding(3);
                if ("CRITICAL".equals(flag)) valueCell.setBackgroundColor(new Color(255, 230, 230));
                table.addCell(valueCell);
                addCell(table, param.path("unit").asText(""), NORMAL_FONT);
                addCell(table, param.path("rangeText").asText("-"), NORMAL_FONT);
                addFlagCell(table, flag);
            }
            doc.add(table);
        }
    }

    private void addFooter(Document doc, JsonNode s) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        LineSeparator line = new LineSeparator();
        doc.add(new Chunk(line));

        JsonNode verifiedBy = s.path("verifiedBy");
        if (!verifiedBy.isMissingNode()) {
            Paragraph sig = new Paragraph(
                    "Verified by: " + verifiedBy.path("name").asText(), NORMAL_FONT);
            sig.setAlignment(Element.ALIGN_RIGHT);
            doc.add(sig);
        }

        Paragraph released = new Paragraph(
                "Released: " + s.path("releasedAt").asText(), SMALL_FONT);
        released.setAlignment(Element.ALIGN_RIGHT);
        doc.add(released);

        Paragraph end = new Paragraph("--- End of Report ---", SMALL_FONT);
        end.setAlignment(Element.ALIGN_CENTER);
        doc.add(end);
    }

    private String formatValue(JsonNode param) {
        if (!param.path("valueNumber").isNull() && !param.path("valueNumber").isMissingNode()) {
            double v = param.path("valueNumber").asDouble();
            int decimals = param.path("decimals").asInt(2);
            return String.format("%." + decimals + "f", v);
        }
        return param.path("valueText").asText("-");
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(new Color(220, 230, 241));
            cell.setBorder(Rectangle.BOX);
            cell.setPadding(4);
            table.addCell(cell);
        }
        table.setHeaderRows(1);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void addFlagCell(PdfPTable table, String flag) {
        Color bg = switch (flag) {
            case "CRITICAL" -> new Color(255, 50, 50);
            case "HIGH", "LOW" -> new Color(255, 165, 0);
            default -> null;
        };
        Font font = (bg != null) ? FLAG_BOLD : NORMAL_FONT;
        PdfPCell cell = new PdfPCell(new Phrase(flag.isEmpty() ? "-" : flag, font));
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(3);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (bg != null) cell.setBackgroundColor(bg);
        table.addCell(cell);
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Phrase(label + ": ", HEADER_FONT));
        cell.addElement(new Phrase(value, NORMAL_FONT));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        table.addCell(cell);
    }
}

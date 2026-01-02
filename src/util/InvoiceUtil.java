package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import model.Order;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvoiceUtil {

    public static void generateInvoice(Order order, String filePath) {

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font italicFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

            // Header
            Paragraph title = new Paragraph("GREEN GROCER INVOICE\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new LineSeparator());

            // Invoice Info
            Paragraph info = new Paragraph();
            info.add(new Chunk("Invoice ID: ", boldFont));
            info.add(order.getId() + "\n");
            info.add(new Chunk("Order Date: ", boldFont));
            info.add(order.getOrderTime().toString() + "\n");
            info.add(new Chunk("Delivery Date: ", boldFont));
            info.add(order.getDeliveryTime().toString() + "\n\n");
            document.add(info);

            // Customer Info
            document.add(new Paragraph("Customer Information", sectionFont));
            document.add(new LineSeparator());

            document.add(new Paragraph(
                    "Customer: " + order.getCustomerName() + "\n" +
                            "Address: " + order.getCustomerAddress() + "\n" +
                            "Carrier: " + order.getCarrierName() + "\n\n",
                    normalFont
            ));

            // Products Table
            document.add(new Paragraph("Order Details", sectionFont));
            document.add(new LineSeparator());

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{4f, 2f, 2f, 2f});

            addTableHeader(table, "Product");
            addTableHeader(table, "Quantity");
            addTableHeader(table, "Unit Price");
            addTableHeader(table, "Total");

            // ⚠️ Eğer ürünleri string olarak tutuyorsan
            // Şimdilik tek satır halinde gösteriyoruz
            PdfPCell cell = new PdfPCell(new Phrase(order.getProducts()));
            cell.setColspan(4);
            cell.setPadding(8);
            table.addCell(cell);

            document.add(table);

            // Total
            Paragraph total = new Paragraph(
                    "\nTOTAL (incl. VAT): " + order.getTotalCost() + " ₺",
                    boldFont
            );
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Footer
            document.add(new Paragraph(
                    "\n\nThank you for shopping with GreenGrocer!",
                    italicFont
            ));

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTableHeader(PdfPTable table, String text) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setPhrase(new Phrase(text));
        header.setPadding(6);
        table.addCell(header);
    }

}

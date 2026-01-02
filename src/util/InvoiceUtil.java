package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import dao.OrderDAO;
import model.Order;

import java.io.*;
import java.nio.file.*;
import java.util.Base64;

/**
 * Utility class for creating and saving invoice files.
 * It generates a PDF invoice and also stores it as Base64 text (CLOB) in the database.
 */
public class InvoiceUtil {

    /**
     * Creates a PDF invoice for the given order.
     * The invoice is saved to disk and stored in the database as Base64 text.
     *
     * @param order The order information
     * @param filePath The file name for saving the PDF
     * @throws Exception if file creation or DB save fails
     */
    public static void generateInvoice(Order order, String filePath) throws Exception {
        // PDF directory
        Path dir = Paths.get("invoices");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // Full file path
        Path pdfPath = dir.resolve(filePath);

        // Create PDF document
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath.toFile()));
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

        // Invoice info
        Paragraph info = new Paragraph();
        info.add(new Chunk("Invoice ID: ", boldFont));
        info.add(order.getId() + "\n");
        info.add(new Chunk("Order Date: ", boldFont));
        info.add(order.getOrderTime() + "\n");
        info.add(new Chunk("Delivery Date: ", boldFont));
        info.add(order.getDeliveryTime() + "\n\n");
        document.add(info);

        // Customer info
        document.add(new Paragraph("Customer Information", sectionFont));
        document.add(new LineSeparator());
        document.add(new Paragraph(
                "Customer: " + order.getCustomerName() + "\n" +
                        "Address: " + order.getCustomerAddress() + "\n" +
                        "Carrier: " + order.getCarrierName() + "\n\n",
                normalFont
        ));

        // Products
        document.add(new Paragraph("Products", sectionFont));
        document.add(new LineSeparator());
        document.add(new Paragraph(order.getProducts(), normalFont));

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

        // ✅ Convert PDF to Base64 for database
        byte[] pdfBytes = Files.readAllBytes(pdfPath);
        String base64Invoice = Base64.getEncoder().encodeToString(pdfBytes);

        // ✅ Save Base64 string into database (CLOB)
        OrderDAO orderDAO = new OrderDAO();
        orderDAO.saveInvoiceContent(order.getId(), base64Invoice);
    }
}

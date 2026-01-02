package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import model.Order;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InvoiceUtil {

    public static Path generateInvoice(Order order) throws Exception {

        // 📁 invoices klasörü
        Path dir = Paths.get("invoices");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // 📄 dosya yolu
        Path filePath = dir.resolve("invoice_" + order.getId() + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
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
        info.add(order.getOrderTime() + "\n");
        info.add(new Chunk("Delivery Date: ", boldFont));
        info.add(order.getDeliveryTime() + "\n\n");
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

        return filePath;
    }
}

package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import model.Order;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvoiceUtil {

    public static Path generateInvoice(Order order) throws Exception {

        Path invoiceDir = Path.of("invoices");
        if (!Files.exists(invoiceDir)) {
            Files.createDirectories(invoiceDir);
        }

        Path filePath = invoiceDir.resolve(
                "invoice_order_" + order.getId() + ".pdf"
        );

        Document document = new Document();
        PdfWriter.getInstance(
                document,
                new FileOutputStream(filePath.toFile())
        );

        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 12);

        document.add(new Paragraph("GREEN GROCER INVOICE", titleFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Invoice ID: " + order.getId(), bold));
        document.add(new Paragraph("Order Date: " + order.getOrderTime(), normal));
        document.add(new Paragraph("Delivery Date: " + order.getDeliveryTime(), normal));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Customer: " + order.getCustomerName(), bold));
        document.add(new Paragraph("Carrier: " + order.getCarrierName(), normal));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Products:", bold));
        document.add(new Paragraph(order.getProducts(), normal));
        document.add(new Paragraph(" "));

        document.add(new Paragraph(
                "Total Cost (incl. VAT): ₺" + String.format("%.2f", order.getTotalCost()),
                bold
        ));

        document.close();
        return filePath;
    }
}

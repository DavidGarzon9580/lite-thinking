package com.litethinking.platform.inventory.report;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.litethinking.platform.catalog.domain.Empresa;
import com.litethinking.platform.catalog.domain.Producto;
import com.litethinking.platform.catalog.domain.ProductoPrecio;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InventoryPdfGenerator {

    public byte[] build(Empresa empresa, List<Producto> productos) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, output);

            document.open();
            document.add(new Paragraph("Inventario de productos"));
            document.add(new Paragraph("Empresa: " + empresa.getNombre() + " (" + empresa.getNit() + ")"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            addHeader(table, "Código");
            addHeader(table, "Nombre");
            addHeader(table, "Características");
            addHeader(table, "Categorías");
            addHeader(table, "Precios");

            for (Producto producto : productos) {
                table.addCell(producto.getCodigo());
                table.addCell(producto.getNombre());
                table.addCell(producto.getCaracteristicas() != null ? producto.getCaracteristicas() : "-");
                String categorias = producto.getCategorias().stream()
                        .map(categoria -> categoria.getNombre())
                        .collect(Collectors.joining(", "));
                table.addCell(categorias.isBlank() ? "-" : categorias);

                String precios = producto.getPrecios().stream()
                        .map(precio -> formatPrecio(precio))
                        .collect(Collectors.joining("\n"));
                table.addCell(precios.isBlank() ? "-" : precios);
            }

            document.add(table);
            document.close();
            return output.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No fue posible generar el PDF", e);
        } catch (Exception e) {
            throw new IllegalStateException("Error al construir el inventario", e);
        }
    }

    private void addHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text));
        table.addCell(cell);
    }

    private String formatPrecio(ProductoPrecio precio) {
        return precio.getMoneda() + " " + precio.getValor();
    }
}

package com.nufi.ui.controllers;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Lote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

public class ReportesController {

    @FXML private ListView<String> listaReportes;

    private final BaseDatos db = ConexionDB.getInstance();
    private final ObservableList<String> historial =
            FXCollections.observableArrayList();

    // Colores NUFI
    private static final BaseColor VERDE_NUFI =
            new BaseColor(45, 106, 79);
    private static final BaseColor VERDE_CLARO =
            new BaseColor(240, 244, 240);
    private static final BaseColor GRIS_TEXTO =
            new BaseColor(80, 80, 80);

    @FXML
    public void initialize() {
        listaReportes.setItems(historial);
    }

    // =========================================
    // REPORTE COSECHA
    // =========================================
    @FXML
    private void reporteCosecha() {
        File archivo = elegirArchivo("reporte_cosecha");
        if (archivo == null) return;

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc,
                    new FileOutputStream(archivo));
            doc.open();

            agregarEncabezado(doc, "Reporte de Cosecha");

            // Cosechas
            ResultSet rs = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT c.id, c.nombre, c.estado, " +
                                    "c.fecha_inicio, c.fecha_fin, " +
                                    "c.precio_kilo_venta, c.total_venta, " +
                                    "COALESCE(SUM(cl.total_cereza_kg),0) as cereza, " +
                                    "COALESCE(SUM(cl.estimado_pergamino_kg),0) as pergamino " +
                                    "FROM cosecha c " +
                                    "LEFT JOIN cosecha_lotes cl ON c.id=cl.cosecha_id " +
                                    "GROUP BY c.id ORDER BY c.fecha_inicio DESC"
                    );

            while (rs.next()) {
                // Título cosecha
                Paragraph titulo = new Paragraph(
                        "☕ " + rs.getString("nombre"),
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                13, VERDE_NUFI)
                );
                titulo.setSpacingBefore(10);
                doc.add(titulo);

                // Tabla resumen
                PdfPTable tabla = new PdfPTable(2);
                tabla.setWidthPercentage(60);
                tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
                tabla.setSpacingBefore(5);

                agregarFilaTabla(tabla, "Estado",
                        rs.getString("estado"));
                agregarFilaTabla(tabla, "Fecha inicio",
                        rs.getString("fecha_inicio") != null ?
                                rs.getString("fecha_inicio") : "—");
                agregarFilaTabla(tabla, "Fecha fin",
                        rs.getString("fecha_fin") != null ?
                                rs.getString("fecha_fin") : "En curso");
                agregarFilaTabla(tabla, "Total cereza",
                        String.format("%.1f kg",
                                rs.getDouble("cereza")));
                agregarFilaTabla(tabla, "Pergamino estimado",
                        String.format("%.1f kg",
                                rs.getDouble("pergamino")));
                agregarFilaTabla(tabla, "Precio kilo venta",
                        "$" + String.format("%,.0f",
                                rs.getDouble("precio_kilo_venta")));
                agregarFilaTabla(tabla, "Total venta estimado",
                        "$" + String.format("%,.0f",
                                rs.getDouble("total_venta")));
                doc.add(tabla);

                // Detalle por lotes
                Paragraph subtitulo = new Paragraph(
                        "Detalle por lote:",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                11, GRIS_TEXTO)
                );
                subtitulo.setSpacingBefore(8);
                doc.add(subtitulo);

                PdfPTable tablaLotes = new PdfPTable(4);
                tablaLotes.setWidthPercentage(100);
                tablaLotes.setSpacingBefore(4);
                agregarEncabezadoTabla(tablaLotes,
                        "Lote", "Cereza kg",
                        "Pergamino est.", "Estado");

                PreparedStatement ps2 = db.getConexion()
                        .prepareStatement(
                                "SELECT l.nombre, cl.estado, " +
                                        "cl.total_cereza_kg, " +
                                        "cl.estimado_pergamino_kg " +
                                        "FROM cosecha_lotes cl " +
                                        "JOIN lotes l ON cl.lote_id=l.id " +
                                        "WHERE cl.cosecha_id=?"
                        );
                ps2.setInt(1, rs.getInt("id"));
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    agregarFilaTablaSimple(tablaLotes,
                            rs2.getString("nombre"),
                            String.format("%.1f",
                                    rs2.getDouble("total_cereza_kg")),
                            String.format("%.1f",
                                    rs2.getDouble(
                                            "estimado_pergamino_kg")),
                            rs2.getString("estado"));
                }
                doc.add(tablaLotes);
                doc.add(new Paragraph(" "));
                agregarLinea(doc);
            }

            doc.close();
            agregarAlHistorial("☕ Reporte Cosecha", archivo);
            mostrarExito(archivo);

        } catch (Exception e) {
            mostrarError("Error generando PDF: " + e.getMessage());
        }
    }

    // =========================================
    // REPORTE TRABAJADORES
    // =========================================
    @FXML
    private void reporteTrabajadores() {
        File archivo = elegirArchivo("reporte_trabajadores");
        if (archivo == null) return;

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc,
                    new FileOutputStream(archivo));
            doc.open();

            agregarEncabezado(doc, "Reporte de Trabajadores");

            ResultSet rs = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT t.id, t.nombre, t.cedula, t.telefono, " +
                                    "COUNT(j.id) as jornadas, " +
                                    "COALESCE(SUM(j.kilos),0) as kilos, " +
                                    "COALESCE(SUM(j.total_pagar),0) as pagado " +
                                    "FROM trabajadores t " +
                                    "LEFT JOIN jornadas j ON t.id=j.trabajador_id " +
                                    "GROUP BY t.id ORDER BY t.nombre"
                    );

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);
            agregarEncabezadoTabla(tabla,
                    "Trabajador", "Cédula",
                    "Jornadas", "Kg rec.", "Total pagado");

            while (rs.next()) {
                agregarFilaTablaSimple(tabla,
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        String.valueOf(rs.getInt("jornadas")),
                        String.format("%.1f", rs.getDouble("kilos")),
                        "$" + String.format("%,.0f",
                                rs.getDouble("pagado")));
            }
            doc.add(tabla);

            // Detalle jornadas por trabajador
            ResultSet rs2 = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT * FROM trabajadores ORDER BY nombre"
                    );

            while (rs2.next()) {
                doc.add(new Paragraph(" "));
                Paragraph titulo = new Paragraph(
                        "👷 " + rs2.getString("nombre"),
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                12, VERDE_NUFI)
                );
                titulo.setSpacingBefore(8);
                doc.add(titulo);

                PdfPTable tablaJ = new PdfPTable(5);
                tablaJ.setWidthPercentage(100);
                tablaJ.setSpacingBefore(4);
                agregarEncabezadoTabla(tablaJ,
                        "Fecha", "Lote", "Labor",
                        "Kilos", "Total");

                PreparedStatement ps3 = db.getConexion()
                        .prepareStatement(
                                "SELECT j.fecha, l.nombre as lote, " +
                                        "j.tipo_trabajo, j.kilos, j.total_pagar " +
                                        "FROM jornadas j " +
                                        "JOIN lotes l ON j.lote_id=l.id " +
                                        "WHERE j.trabajador_id=? " +
                                        "ORDER BY j.fecha DESC"
                        );
                ps3.setInt(1, rs2.getInt("id"));
                ResultSet rs3 = ps3.executeQuery();

                boolean tieneJornadas = false;
                while (rs3.next()) {
                    tieneJornadas = true;
                    agregarFilaTablaSimple(tablaJ,
                            rs3.getString("fecha"),
                            rs3.getString("lote"),
                            rs3.getString("tipo_trabajo"),
                            String.format("%.1f",
                                    rs3.getDouble("kilos")),
                            "$" + String.format("%,.0f",
                                    rs3.getDouble("total_pagar")));
                }

                if (tieneJornadas) {
                    doc.add(tablaJ);
                } else {
                    doc.add(new Paragraph(
                            "Sin jornadas registradas",
                            FontFactory.getFont(
                                    FontFactory.HELVETICA,
                                    10, GRIS_TEXTO)
                    ));
                }
                agregarLinea(doc);
            }

            doc.close();
            agregarAlHistorial("👷 Reporte Trabajadores", archivo);
            mostrarExito(archivo);

        } catch (Exception e) {
            mostrarError("Error generando PDF: " + e.getMessage());
        }
    }

    // =========================================
    // REPORTE INVENTARIO
    // =========================================
    @FXML
    private void reporteInventario() {
        File archivo = elegirArchivo("reporte_inventario");
        if (archivo == null) return;

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc,
                    new FileOutputStream(archivo));
            doc.open();

            agregarEncabezado(doc, "Reporte de Inventario");

            // Productos
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);
            agregarEncabezadoTabla(tabla,
                    "Producto", "Tipo", "Unidad",
                    "Stock", "Mínimo", "Estado");

            ResultSet rs = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT * FROM productos_bodega WHERE activo=1"
                    );

            while (rs.next()) {
                double stock  = rs.getDouble("stock_actual");
                double minimo = rs.getDouble("stock_minimo");
                String estado = stock <= minimo ?
                        "⚠ BAJO" : "OK";
                agregarFilaTablaSimple(tabla,
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("unidad_medida"),
                        String.format("%.1f", stock),
                        String.format("%.1f", minimo),
                        estado);
            }
            doc.add(tabla);

            // Movimientos recientes
            doc.add(new Paragraph(" "));
            Paragraph subtitulo = new Paragraph(
                    "Movimientos recientes:",
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12, VERDE_NUFI)
            );
            subtitulo.setSpacingBefore(8);
            doc.add(subtitulo);

            PdfPTable tablaM = new PdfPTable(5);
            tablaM.setWidthPercentage(100);
            tablaM.setSpacingBefore(4);
            agregarEncabezadoTabla(tablaM,
                    "Fecha", "Producto", "Tipo",
                    "Cantidad", "Observaciones");

            ResultSet rs2 = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT mb.fecha, pb.nombre, " +
                                    "mb.tipo_movimiento, mb.cantidad, " +
                                    "mb.observaciones " +
                                    "FROM movimientos_bodega mb " +
                                    "JOIN productos_bodega pb " +
                                    "ON mb.producto_id=pb.id " +
                                    "ORDER BY mb.fecha DESC LIMIT 50"
                    );

            while (rs2.next()) {
                agregarFilaTablaSimple(tablaM,
                        rs2.getString("fecha"),
                        rs2.getString("nombre"),
                        rs2.getString("tipo_movimiento"),
                        String.format("%.1f",
                                rs2.getDouble("cantidad")),
                        rs2.getString("observaciones") != null ?
                                rs2.getString("observaciones") : "—");
            }
            doc.add(tablaM);

            doc.close();
            agregarAlHistorial("📦 Reporte Inventario", archivo);
            mostrarExito(archivo);

        } catch (Exception e) {
            mostrarError("Error generando PDF: " + e.getMessage());
        }
    }

    // =========================================
    // REPORTE LOTE
    // =========================================
    @FXML
    private void reporteLote() {
        List<Lote> lotes = db.obtenerLotesConKilos();
        if (lotes.isEmpty()) {
            mostrarError("No hay lotes registrados.");
            return;
        }

        // Elegir lote
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                lotes.get(0).nombre,
                lotes.stream().map(l -> l.nombre).toList()
        );
        dialog.setTitle("Seleccionar lote");
        dialog.setHeaderText("¿De qué lote deseas el reporte?");
        dialog.setContentText("Lote:");

        dialog.showAndWait().ifPresent(nombreLote -> {
            Lote lote = lotes.stream()
                    .filter(l -> l.nombre.equals(nombreLote))
                    .findFirst().orElse(null);
            if (lote == null) return;

            File archivo = elegirArchivo(
                    "reporte_lote_" + lote.nombre.toLowerCase());
            if (archivo == null) return;

            try {
                Document doc = new Document(PageSize.A4);
                PdfWriter.getInstance(doc,
                        new FileOutputStream(archivo));
                doc.open();

                agregarEncabezado(doc,
                        "Historial Lote: " + lote.nombre);

                // Info lote
                PdfPTable info = new PdfPTable(2);
                info.setWidthPercentage(60);
                info.setHorizontalAlignment(Element.ALIGN_LEFT);
                info.setSpacingBefore(5);
                agregarFilaTabla(info, "Matas",
                        String.valueOf(lote.matas));
                agregarFilaTabla(info, "Fecha siembra",
                        lote.fechaSiembra);
                agregarFilaTabla(info, "Kg cosechados",
                        String.format("%.1f",
                                lote.kilosCosechados));
                doc.add(info);

                // Jornadas
                doc.add(new Paragraph(" "));
                Paragraph tJornadas = new Paragraph(
                        "Jornadas de trabajo:",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                12, VERDE_NUFI)
                );
                tJornadas.setSpacingBefore(8);
                doc.add(tJornadas);

                PdfPTable tablaJ = new PdfPTable(5);
                tablaJ.setWidthPercentage(100);
                tablaJ.setSpacingBefore(4);
                agregarEncabezadoTabla(tablaJ,
                        "Fecha", "Trabajador",
                        "Labor", "Kilos", "Total");

                PreparedStatement ps = db.getConexion()
                        .prepareStatement(
                                "SELECT j.fecha, t.nombre, " +
                                        "j.tipo_trabajo, j.kilos, j.total_pagar " +
                                        "FROM jornadas j " +
                                        "JOIN trabajadores t ON j.trabajador_id=t.id " +
                                        "WHERE j.lote_id=? ORDER BY j.fecha DESC"
                        );
                ps.setInt(1, lote.id);
                ResultSet rs = ps.executeQuery();

                double totalPagado = 0;
                while (rs.next()) {
                    agregarFilaTablaSimple(tablaJ,
                            rs.getString("fecha"),
                            rs.getString("nombre"),
                            rs.getString("tipo_trabajo"),
                            String.format("%.1f",
                                    rs.getDouble("kilos")),
                            "$" + String.format("%,.0f",
                                    rs.getDouble("total_pagar")));
                    totalPagado += rs.getDouble("total_pagar");
                }
                doc.add(tablaJ);

                // Insumos
                doc.add(new Paragraph(" "));
                Paragraph tInsumos = new Paragraph(
                        "Insumos utilizados:",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                12, VERDE_NUFI)
                );
                tInsumos.setSpacingBefore(8);
                doc.add(tInsumos);

                PdfPTable tablaI = new PdfPTable(4);
                tablaI.setWidthPercentage(100);
                tablaI.setSpacingBefore(4);
                agregarEncabezadoTabla(tablaI,
                        "Fecha", "Producto",
                        "Cantidad", "Tipo");

                PreparedStatement ps2 = db.getConexion()
                        .prepareStatement(
                                "SELECT mb.fecha, pb.nombre, " +
                                        "mb.cantidad, mb.tipo_movimiento " +
                                        "FROM movimientos_bodega mb " +
                                        "JOIN productos_bodega pb " +
                                        "ON mb.producto_id=pb.id " +
                                        "WHERE mb.lote_id=? ORDER BY mb.fecha DESC"
                        );
                ps2.setInt(1, lote.id);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    agregarFilaTablaSimple(tablaI,
                            rs2.getString("fecha"),
                            rs2.getString("nombre"),
                            String.format("%.1f",
                                    rs2.getDouble("cantidad")),
                            rs2.getString("tipo_movimiento"));
                }
                doc.add(tablaI);

                // Resumen final
                doc.add(new Paragraph(" "));
                agregarLinea(doc);
                Paragraph resumen = new Paragraph(
                        "Total pagado en jornales: $" +
                                String.format("%,.0f", totalPagado),
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                12, VERDE_NUFI)
                );
                resumen.setSpacingBefore(8);
                doc.add(resumen);

                doc.close();
                agregarAlHistorial(
                        "🌿 Reporte Lote " + lote.nombre, archivo);
                mostrarExito(archivo);

            } catch (Exception e) {
                mostrarError("Error generando PDF: "
                        + e.getMessage());
            }
        });
    }

    // =========================================
    // HELPERS PDF
    // =========================================
    private void agregarEncabezado(Document doc, String titulo)
            throws DocumentException {
        // Logo finca
        Paragraph finca = new Paragraph(
                "☕ FINCA LA QUINTA",
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD, 20, VERDE_NUFI)
        );
        finca.setAlignment(Element.ALIGN_CENTER);
        doc.add(finca);

        Paragraph ubicacion = new Paragraph(
                "Vereda Cordoncillal · Albania, Santander, Colombia",
                FontFactory.getFont(
                        FontFactory.HELVETICA, 10, GRIS_TEXTO)
        );
        ubicacion.setAlignment(Element.ALIGN_CENTER);
        doc.add(ubicacion);

        doc.add(new Paragraph(" "));

        // Título reporte
        Paragraph tituloP = new Paragraph(
                titulo,
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK)
        );
        tituloP.setAlignment(Element.ALIGN_CENTER);
        tituloP.setSpacingBefore(5);
        doc.add(tituloP);

        // Fecha generación
        Paragraph fecha = new Paragraph(
                "Generado: " + LocalDate.now(),
                FontFactory.getFont(
                        FontFactory.HELVETICA, 9, GRIS_TEXTO)
        );
        fecha.setAlignment(Element.ALIGN_CENTER);
        doc.add(fecha);

        doc.add(new Paragraph(" "));
        agregarLinea(doc);
    }

    private void agregarEncabezadoTabla(
            PdfPTable tabla, String... columnas) {
        for (String col : columnas) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(col, FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10, BaseColor.WHITE))
            );
            cell.setBackgroundColor(VERDE_NUFI);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }
    }

    private void agregarFilaTablaSimple(
            PdfPTable tabla, String... valores) {
        for (String val : valores) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(val != null ? val : "—",
                            FontFactory.getFont(
                                    FontFactory.HELVETICA, 9,
                                    GRIS_TEXTO))
            );
            cell.setPadding(5);
            cell.setBackgroundColor(VERDE_CLARO);
            tabla.addCell(cell);
        }
    }

    private void agregarFilaTabla(
            PdfPTable tabla, String clave, String valor) {
        PdfPCell cellClave = new PdfPCell(
                new Phrase(clave, FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10, GRIS_TEXTO))
        );
        cellClave.setPadding(5);
        cellClave.setBackgroundColor(VERDE_CLARO);
        tabla.addCell(cellClave);

        PdfPCell cellValor = new PdfPCell(
                new Phrase(valor != null ? valor : "—",
                        FontFactory.getFont(
                                FontFactory.HELVETICA, 10, GRIS_TEXTO))
        );
        cellValor.setPadding(5);
        tabla.addCell(cellValor);
    }

    private void agregarLinea(Document doc)
            throws DocumentException {
        LineSeparator linea = new LineSeparator();
        linea.setLineColor(VERDE_NUFI);
        doc.add(new Chunk(linea));
    }

    // =========================================
    // HELPERS UI
    // =========================================
    private File elegirArchivo(String nombreSugerido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte PDF");
        chooser.setInitialFileName(
                nombreSugerido + "_" +
                        LocalDate.now() + ".pdf");

        // ✅ Buscar carpeta Descargas en Windows español e inglés
        String userHome = System.getProperty("user.home");
        File carpetaDescargas = new File(userHome + "\\Downloads");

        // Si no existe en inglés busca en español
        if (!carpetaDescargas.exists()) {
            carpetaDescargas = new File(userHome + "\\Descargas");
        }
        // Si tampoco existe usa el home del usuario
        if (!carpetaDescargas.exists()) {
            carpetaDescargas = new File(userHome);
        }

        chooser.setInitialDirectory(carpetaDescargas);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "PDF files", "*.pdf"));
        Stage stage = new Stage();
        return chooser.showSaveDialog(stage);
    }

    private void mostrarExito(File archivo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF generado");
        alert.setHeaderText("✅ Reporte generado exitosamente");
        alert.setContentText("Guardado en:\n" +
                archivo.getAbsolutePath());
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("❌ Error al generar PDF");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void agregarAlHistorial(String tipo, File archivo) {
        historial.add(0, "📄 " + tipo + " — " +
                LocalDate.now() + " → " +
                archivo.getName());
    }
}
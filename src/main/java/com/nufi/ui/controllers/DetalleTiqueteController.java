package com.nufi.ui.controllers;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import javafx.fxml.FXML;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;

/**
 * Controlador de la ventana de detalle de un tiquete de pago.
 * Muestra el recibo en pantalla y permite imprimirlo o descargarlo en PDF.
 */
public class DetalleTiqueteController {

    @FXML private VBox  raizTiquete;
    @FXML private Label lblNumeroTiquete;
    @FXML private Label lblFechaPago;
    @FXML private Label lblTrabajador;
    @FXML private Label lblLote;
    @FXML private Label lblLabor;
    @FXML private Label lblModoPago;
    @FXML private Label lblKilos;
    @FXML private Label lblTotal;
    @FXML private Label lblEstado;
    @FXML private Label lblMedioPago;
    @FXML private Label lblObservaciones;
    @FXML private HBox  panelKilos;
    @FXML private VBox  panelObservaciones;

    // Datos crudos guardados para generar el PDF / impresión.
    private String dNumero = "";
    private String dTrabajador = "";
    private String dCedula = "";
    private String dFechaPago = "";
    private String dLabor = "";
    private String dLote = "";
    private String dModoPago = "";
    private String dKilos = "";
    private String dTotal = "";
    private String dEstado = "";
    private String dMedioPago = "";
    private String dObservaciones = "";

    // Colores NUFI (mismos del módulo de reportes)
    private static final BaseColor VERDE_NUFI  = new BaseColor(45, 106, 79);
    private static final BaseColor VERDE_CLARO = new BaseColor(240, 244, 240);
    private static final BaseColor GRIS_TEXTO  = new BaseColor(80, 80, 80);

    /**
     * Carga los datos del tiquete en la vista y los guarda para PDF/impresión.
     */
    public void cargarDatos(String numero,    String trabajador,
                            String fechaPago, String labor,
                            String lote,      String modoPago,
                            String kilos,     String total,
                            String estado,    String medioPago,
                            String observaciones, String cedula) {

        // Guardar crudos
        this.dNumero        = numero != null ? numero : "";
        this.dTrabajador    = trabajador != null ? trabajador : "";
        this.dCedula        = cedula != null ? cedula : "";
        this.dFechaPago     = fechaPago != null ? fechaPago : "";
        this.dLabor         = labor != null ? labor : "";
        this.dLote          = lote != null ? lote : "";
        this.dModoPago      = modoPago != null ? modoPago : "";
        this.dKilos         = kilos != null ? kilos : "";
        this.dTotal         = total != null ? total : "";
        this.dEstado        = estado != null ? estado : "";
        this.dMedioPago     = medioPago != null ? medioPago : "";
        this.dObservaciones = observaciones != null ? observaciones : "";

        lblNumeroTiquete.setText("TIQUETE DE PAGO " + dNumero);
        lblFechaPago.setText("Fecha: " + dFechaPago);
        lblTrabajador.setText(dTrabajador);
        lblLote.setText(dLote);
        lblLabor.setText(dLabor.toUpperCase());
        lblModoPago.setText(dModoPago.equals("kilo") ? "Por kilo" : "Por día");
        lblTotal.setText(dTotal);

        // Medio de pago (efectivo / transferencia / nequi...)
        lblMedioPago.setText(dMedioPago.isBlank() ? "—" : dMedioPago);

        // Observaciones: ocultar el bloque si no hay
        if (dObservaciones.isBlank()) {
            panelObservaciones.setVisible(false);
            panelObservaciones.setManaged(false);
        } else {
            lblObservaciones.setText(dObservaciones);
            panelObservaciones.setVisible(true);
            panelObservaciones.setManaged(true);
        }

        // Mostrar kilos solo si pago por kilo
        if (dModoPago.equals("kilo") && !dKilos.equals("0.0")) {
            lblKilos.setText(dKilos + " kg");
            panelKilos.setVisible(true);
            panelKilos.setManaged(true);
        } else {
            panelKilos.setVisible(false);
            panelKilos.setManaged(false);
        }

        // Estado con color
        if (dEstado.contains("Pagado")) {
            lblEstado.setText("✅ PAGADO");
            lblEstado.setStyle(
                    "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#2d6a4f;");
        } else {
            lblEstado.setText("⏳ PENDIENTE");
            lblEstado.setStyle(
                    "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#c1121f;");
        }
    }

    // =====================================================================
    //  IMPRIMIR (envía el recibo a la impresora — sirve para térmica POS)
    // =====================================================================
    @FXML
    private void imprimir() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            mostrarError("No se encontró ninguna impresora instalada.");
            return;
        }

        Window ventana = raizTiquete.getScene().getWindow();
        if (!job.showPrintDialog(ventana)) {
            return; // El usuario canceló.
        }

        // Escalar el recibo para que quepa en el área imprimible seleccionada.
        Scale escala = null;
        try {
            PageLayout layout = job.getJobSettings().getPageLayout();
            double anchoNodo = raizTiquete.getBoundsInParent().getWidth();
            double altoNodo  = raizTiquete.getBoundsInParent().getHeight();
            double factor = Math.min(
                    layout.getPrintableWidth()  / anchoNodo,
                    layout.getPrintableHeight() / altoNodo);
            if (factor < 1.0) {
                escala = new Scale(factor, factor);
                raizTiquete.getTransforms().add(escala);
            }

            boolean impreso = job.printPage(raizTiquete);
            if (impreso) {
                job.endJob();
            } else {
                mostrarError("No se pudo imprimir el tiquete.");
            }
        } finally {
            // Restaurar el tamaño en pantalla.
            if (escala != null) {
                raizTiquete.getTransforms().remove(escala);
            }
        }
    }

    // =====================================================================
    //  DESCARGAR PDF (recibo A4, se guarda donde el usuario elija)
    // =====================================================================
    @FXML
    private void descargarPdf() {
        File archivo = elegirArchivo("tiquete_" + dNumero.replace("N°", "").trim());
        if (archivo == null) {
            return; // Cancelado.
        }

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, new FileOutputStream(archivo));
            doc.open();

            // Encabezado
            Paragraph finca = new Paragraph("☕ FINCA LA QUINTA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, VERDE_NUFI));
            finca.setAlignment(Element.ALIGN_CENTER);
            doc.add(finca);

            Paragraph ubic = new Paragraph(
                    "Vereda Cordoncillal · Albania, Santander, Colombia",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS_TEXTO));
            ubic.setAlignment(Element.ALIGN_CENTER);
            doc.add(ubic);

            doc.add(new Paragraph(" "));

            Paragraph titulo = new Paragraph("TIQUETE DE PAGO " + dNumero,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK));
            titulo.setAlignment(Element.ALIGN_CENTER);
            doc.add(titulo);

            Paragraph fechaP = new Paragraph("Fecha de pago: " + dFechaPago,
                    FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS_TEXTO));
            fechaP.setAlignment(Element.ALIGN_CENTER);
            doc.add(fechaP);

            doc.add(new Paragraph(" "));
            LineSeparator linea = new LineSeparator();
            linea.setLineColor(VERDE_NUFI);
            doc.add(new Chunk(linea));
            doc.add(new Paragraph(" "));

            // Tabla de datos
            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(80);
            tabla.setWidths(new float[]{1.2f, 2f});

            agregarFila(tabla, "Trabajador:", dTrabajador);
            if (!dCedula.isBlank()) {
                agregarFila(tabla, "Cédula:", dCedula);
            }
            agregarFila(tabla, "Lote:", dLote);
            agregarFila(tabla, "Labor:", dLabor.toUpperCase());
            agregarFila(tabla, "Tipo de pago:",
                    dModoPago.equals("kilo") ? "Por kilo" : "Por día");
            if (dModoPago.equals("kilo") && !dKilos.equals("0.0")) {
                agregarFila(tabla, "Kilos cereza:", dKilos + " kg");
            }
            agregarFila(tabla, "Medio de pago:",
                    dMedioPago.isBlank() ? "—" : dMedioPago);
            if (!dObservaciones.isBlank()) {
                agregarFila(tabla, "Observaciones:", dObservaciones);
            }
            doc.add(tabla);

            doc.add(new Paragraph(" "));

            // Bloque TOTAL
            PdfPTable tablaTotal = new PdfPTable(2);
            tablaTotal.setWidthPercentage(80);
            tablaTotal.setWidths(new float[]{1.2f, 2f});

            PdfPCell cTotalLabel = new PdfPCell(new Phrase("TOTAL PAGADO",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BaseColor.WHITE)));
            cTotalLabel.setBackgroundColor(VERDE_NUFI);
            cTotalLabel.setPadding(8);
            cTotalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            tablaTotal.addCell(cTotalLabel);

            PdfPCell cTotalValor = new PdfPCell(new Phrase(dTotal,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, VERDE_NUFI)));
            cTotalValor.setPadding(8);
            cTotalValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tablaTotal.addCell(cTotalValor);
            doc.add(tablaTotal);

            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            Paragraph firma = new Paragraph("Firma / Recibido: ______________________",
                    FontFactory.getFont(FontFactory.HELVETICA, 11, GRIS_TEXTO));
            firma.setAlignment(Element.ALIGN_CENTER);
            doc.add(firma);

            doc.add(new Paragraph(" "));
            Paragraph pie = new Paragraph("Sistema NUFI — Finca La Quinta",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, GRIS_TEXTO));
            pie.setAlignment(Element.ALIGN_CENTER);
            doc.add(pie);

            doc.close();

            mostrarExito(archivo);
            abrirArchivo(archivo);

        } catch (Exception e) {
            mostrarError("No se pudo generar el PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================================
    //  Helpers
    // =====================================================================
    private void agregarFila(PdfPTable tabla, String clave, String valor) {
        PdfPCell cClave = new PdfPCell(new Phrase(clave,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, GRIS_TEXTO)));
        cClave.setPadding(6);
        cClave.setBackgroundColor(VERDE_CLARO);
        tabla.addCell(cClave);

        PdfPCell cValor = new PdfPCell(new Phrase(valor != null ? valor : "—",
                FontFactory.getFont(FontFactory.HELVETICA, 11, GRIS_TEXTO)));
        cValor.setPadding(6);
        tabla.addCell(cValor);
    }

    private File elegirArchivo(String nombreSugerido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar tiquete PDF");
        chooser.setInitialFileName(nombreSugerido + ".pdf");

        String userHome = System.getProperty("user.home");
        File descargas = new File(userHome + "\\Downloads");
        if (!descargas.exists()) {
            descargas = new File(userHome + "\\Descargas");
        }
        if (!descargas.exists()) {
            descargas = new File(userHome);
        }
        chooser.setInitialDirectory(descargas);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        Window ventana = raizTiquete.getScene().getWindow();
        return chooser.showSaveDialog(ventana);
    }

    private void abrirArchivo(File archivo) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(archivo);
            }
        } catch (Exception e) {
            System.out.println("No se pudo abrir el PDF automáticamente: " + e.getMessage());
        }
    }

    private void mostrarExito(File archivo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("PDF generado");
        alert.setHeaderText("✅ Tiquete guardado exitosamente");
        alert.setContentText("Guardado en:\n" + archivo.getAbsolutePath());
        alert.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("❌ Ocurrió un problema");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) lblNumeroTiquete.getScene().getWindow();
        stage.close();
    }
}
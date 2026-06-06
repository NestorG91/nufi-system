package com.nufi.ui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class DetalleTiqueteController {

    @FXML private Label lblNumeroTiquete;
    @FXML private Label lblFechaPago;
    @FXML private Label lblTrabajador;
    @FXML private Label lblLote;
    @FXML private Label lblLabor;
    @FXML private Label lblModoPago;
    @FXML private Label lblKilos;
    @FXML private Label lblTotal;
    @FXML private Label lblEstado;
    @FXML private HBox  panelKilos;

    public void cargarDatos(String numero,    String trabajador,
                            String fechaPago, String labor,
                            String lote,      String modoPago,
                            String kilos,     String total,
                            String estado) {

        lblNumeroTiquete.setText("TIQUETE DE PAGO " + numero);
        lblFechaPago.setText("Fecha: " + fechaPago);
        lblTrabajador.setText(trabajador);
        lblLote.setText(lote);
        lblLabor.setText(labor.toUpperCase());
        lblModoPago.setText(modoPago.equals("kilo") ?
                "Por kilo" : "Por día");
        lblTotal.setText(total);

        // Mostrar kilos solo si pago por kilo
        if (modoPago.equals("kilo") && !kilos.equals("0.0")) {
            lblKilos.setText(kilos + " kg");
            panelKilos.setVisible(true);
            panelKilos.setManaged(true);
        } else {
            panelKilos.setVisible(false);
            panelKilos.setManaged(false);
        }

        // Estado con color
        if (estado.contains("Pagado")) {
            lblEstado.setText("✅ PAGADO");
            lblEstado.setStyle(
                    "-fx-font-size:15px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-text-fill:#2d6a4f;");
        } else {
            lblEstado.setText("⏳ PENDIENTE");
            lblEstado.setStyle(
                    "-fx-font-size:15px;" +
                            "-fx-font-weight:bold;" +
                            "-fx-text-fill:#c1121f;");
        }
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) lblNumeroTiquete
                .getScene().getWindow();
        stage.close();
    }
}
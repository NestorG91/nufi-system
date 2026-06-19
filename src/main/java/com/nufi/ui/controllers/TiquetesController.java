package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Trabajador;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TiquetesController {

    @FXML private TableView<ObservableList<String>> tablaTiquetes;
    @FXML private TableColumn<ObservableList<String>, String> colNumero;
    @FXML private TableColumn<ObservableList<String>, String> colTrabajador;
    @FXML private TableColumn<ObservableList<String>, String> colFecha;
    @FXML private TableColumn<ObservableList<String>, String> colLabor;
    @FXML private TableColumn<ObservableList<String>, String> colLote;
    @FXML private TableColumn<ObservableList<String>, String> colTotal;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;
    @FXML private TableColumn<ObservableList<String>, Void>   colAcciones;

    @FXML private ComboBox<String> cmbFiltroTrabajador;
    @FXML private VBox             panelPago;
    @FXML private ComboBox<String> cmbTrabajador;
    @FXML private DatePicker       dpFechaPago;
    @FXML private VBox             panelJornadas;
    @FXML private Label            lblTotalPagar;
    @FXML private Label            lblError;
    @FXML private ComboBox<String> cmbMedioPago;
    @FXML private TextArea         txtObservaciones;

    private final BaseDatos    db = ConexionDB.getInstance();
    private List<Trabajador>   listaTrabajadores;
    private List<CheckBox>     checkboxJornadas = new ArrayList<>();
    private List<Integer>      idsJornadas      = new ArrayList<>();
    private List<Double>       totalesJornadas  = new ArrayList<>();

    @FXML
    public void initialize() {
        tablaTiquetes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas();
        cargarFiltroTrabajadores();
        cargarTiquetes(null);
    }

    private void configurarColumnas() {
        colNumero.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(0)));
        colTrabajador.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(1)));
        colFecha.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(2)));
        colLabor.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(3)));
        colLote.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(4)));
        colTotal.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(5)));
        colEstado.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(6)));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("🧾 Ver");
            private final HBox box = new HBox(btnVer);
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnVer.setStyle(
                        "-fx-background-color:#2d6a4f;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:4 10;" +
                                "-fx-cursor:hand;");
                btnVer.setOnAction(e -> {
                    ObservableList<String> fila = getTableView()
                            .getItems().get(getIndex());
                    verTiquete(fila);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void cargarFiltroTrabajadores() {
        listaTrabajadores = db.obtenerTrabajadores();
        ObservableList<String> nombres =
                FXCollections.observableArrayList("Todos");
        for (Trabajador t : listaTrabajadores) {
            nombres.add(t.nombre);
        }
        cmbFiltroTrabajador.setItems(nombres);
        cmbTrabajador.setItems(nombres.filtered(
                s -> !s.equals("Todos")));
    }

    private void cargarTiquetes(Integer trabajadorId) {
        ObservableList<ObservableList<String>> datos =
                FXCollections.observableArrayList();
        try {
            String sql =
                    "SELECT t.numero_tiquete, tr.nombre, t.fecha_pago, " +
                            "j.tipo_trabajo, l.nombre as lote, " +
                            "t.total_pagado, t.impreso " +
                            "FROM tiquetes t " +
                            "JOIN trabajadores tr ON t.trabajador_id = tr.id " +
                            "JOIN jornadas j ON t.jornada_id = j.id " +
                            "JOIN lotes l ON j.lote_id = l.id ";

            if (trabajadorId != null) {
                sql += "WHERE t.trabajador_id = ? ";
            }
            sql += "ORDER BY t.numero_tiquete DESC";

            PreparedStatement ps = db.getConexion().prepareStatement(sql);
            if (trabajadorId != null) ps.setInt(1, trabajadorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ObservableList<String> fila =
                        FXCollections.observableArrayList();
                fila.add("N°" + rs.getInt("numero_tiquete"));
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("fecha_pago") != null ?
                        rs.getString("fecha_pago") : "—");
                fila.add(rs.getString("tipo_trabajo"));
                fila.add(rs.getString("lote"));
                fila.add("$" + String.format("%,.0f",
                        rs.getDouble("total_pagado")));
                fila.add(rs.getInt("impreso") == 1 ?
                        "✅ Pagado" : "⏳ Pendiente");
                datos.add(fila);
            }
        } catch (Exception e) {
            System.out.println("❌ Error tiquetes: " + e.getMessage());
        }
        tablaTiquetes.setItems(datos);
    }

    @FXML
    private void filtrar() {
        String seleccionado = cmbFiltroTrabajador.getValue();
        if (seleccionado == null || seleccionado.equals("Todos")) {
            cargarTiquetes(null);
            return;
        }
        listaTrabajadores.stream()
                .filter(t -> t.nombre.equals(seleccionado))
                .findFirst()
                .ifPresent(t -> cargarTiquetes(t.id));
    }

    @FXML
    private void limpiarFiltro() {
        cmbFiltroTrabajador.setValue("Todos");
        cargarTiquetes(null);
    }

    @FXML
    private void abrirGenerarPago() {
        cmbTrabajador.setValue(null);
        dpFechaPago.setValue(LocalDate.now());

        // Opciones de medio de pago (solo se llenan una vez)
        if (cmbMedioPago.getItems().isEmpty()) {
            cmbMedioPago.getItems().addAll(
                    "Efectivo", "Transferencia", "Nequi", "Daviplata", "Otro");
        }
        cmbMedioPago.setValue(null);
        txtObservaciones.clear();

        panelJornadas.getChildren().clear();
        checkboxJornadas.clear();
        idsJornadas.clear();
        totalesJornadas.clear();
        lblTotalPagar.setText("$0");
        ocultarError();

        // Listener para cargar jornadas al seleccionar trabajador
        cmbTrabajador.valueProperty().addListener(
                (obs, old, nuevo) -> {
                    if (nuevo != null) cargarJornadasPendientes(nuevo);
                });

        panelPago.setVisible(true);
        panelPago.setManaged(true);
    }

    private void cargarJornadasPendientes(String nombreTrabajador) {
        panelJornadas.getChildren().clear();
        checkboxJornadas.clear();
        idsJornadas.clear();
        totalesJornadas.clear();
        lblTotalPagar.setText("$0");

        listaTrabajadores.stream()
                .filter(t -> t.nombre.equals(nombreTrabajador))
                .findFirst()
                .ifPresent(trab -> {
                    try {
                        // Jornadas sin tiquete
                        PreparedStatement ps = db.getConexion()
                                .prepareStatement(
                                        "SELECT j.id, l.nombre as lote, j.fecha, " +
                                                "j.tipo_trabajo, j.total_pagar " +
                                                "FROM jornadas j " +
                                                "JOIN lotes l ON j.lote_id = l.id " +
                                                "WHERE j.trabajador_id = ? " +
                                                "AND j.id NOT IN " +
                                                "(SELECT jornada_id FROM tiquetes) " +
                                                "ORDER BY j.fecha DESC"
                                );
                        ps.setInt(1, trab.id);
                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            double total = rs.getDouble("total_pagar");
                            String label = rs.getString("fecha") +
                                    " | " + rs.getString("lote") +
                                    " | " + rs.getString("tipo_trabajo") +
                                    " | $" + String.format("%,.0f", total);

                            CheckBox cb = new CheckBox(label);
                            cb.setStyle("-fx-font-size:12px;");
                            cb.selectedProperty().addListener(
                                    (obs, old, val) -> recalcularTotal());

                            checkboxJornadas.add(cb);
                            idsJornadas.add(rs.getInt("id"));
                            totalesJornadas.add(total);
                            panelJornadas.getChildren().add(cb);
                        }

                        if (panelJornadas.getChildren().isEmpty()) {
                            panelJornadas.getChildren().add(
                                    new Label("✅ Sin jornadas pendientes de pago")
                            );
                        }

                    } catch (Exception e) {
                        System.out.println("❌ Error jornadas pendientes: "
                                + e.getMessage());
                    }
                });
    }

    private void recalcularTotal() {
        double total = 0;
        for (int i = 0; i < checkboxJornadas.size(); i++) {
            if (checkboxJornadas.get(i).isSelected()) {
                total += totalesJornadas.get(i);
            }
        }
        lblTotalPagar.setText("$" + String.format("%,.0f", total));
    }

    @FXML
    private void generarTiquete() {
        if (cmbTrabajador.getValue() == null) {
            mostrarError("Selecciona un trabajador.");
            return;
        }

        List<Integer> seleccionadas = new ArrayList<>();
        for (int i = 0; i < checkboxJornadas.size(); i++) {
            if (checkboxJornadas.get(i).isSelected()) {
                seleccionadas.add(idsJornadas.get(i));
            }
        }

        if (seleccionadas.isEmpty()) {
            mostrarError("Selecciona al menos una jornada.");
            return;
        }

        String fechaPago = dpFechaPago.getValue().toString();
        Trabajador trab = listaTrabajadores.stream()
                .filter(t -> t.nombre.equals(cmbTrabajador.getValue()))
                .findFirst().orElse(null);

        if (trab == null) return;

        double totalGeneral = 0;
        for (int i = 0; i < checkboxJornadas.size(); i++) {
            if (checkboxJornadas.get(i).isSelected()) {
                totalGeneral += totalesJornadas.get(i);
            }
        }

        // Datos de pago capturados en el panel
        String medioPago = cmbMedioPago.getValue() != null
                ? cmbMedioPago.getValue() : "";
        String observaciones = txtObservaciones.getText() != null
                ? txtObservaciones.getText().trim() : "";

        // Generar tiquete por cada jornada seleccionada
        for (int jornadaId : seleccionadas) {
            guardarTiquete(jornadaId, trab.id, fechaPago,
                    totalGeneral / seleccionadas.size(),
                    medioPago, observaciones);
        }

        // Mostrar resumen
        mostrarResumenTiquete(trab.nombre, fechaPago,
                totalGeneral, seleccionadas.size());

        cancelar();
        cargarTiquetes(null);
    }

    private void guardarTiquete(int jornadaId, int trabajadorId,
                                String fecha, double total,
                                String medioPago, String observaciones) {
        try {
            int numero = db.siguienteNumeroTiquete();
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "INSERT INTO tiquetes (numero_tiquete, jornada_id, " +
                            "trabajador_id, fecha_pago, total_pagado, " +
                            "medio_pago, observaciones, impreso) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, 1)"
            );
            ps.setInt(1, numero);
            ps.setInt(2, jornadaId);
            ps.setInt(3, trabajadorId);
            ps.setString(4, fecha);
            ps.setDouble(5, total);
            ps.setString(6, medioPago);
            ps.setString(7, observaciones);
            ps.executeUpdate();
        } catch (Exception e) {
            // No silenciar: si la BD aun no tiene las columnas medio_pago u
            // observaciones (por migracion fallida) el usuario lo veria como
            // "el tiquete no se guarda". Mostrar Alert para que sea evidente.
            System.out.println("❌ Error guardar tiquete: " + e.getMessage());
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error al guardar tiquete");
            err.setHeaderText("❌ No se pudo guardar el tiquete");
            err.setContentText(
                    "Detalle: " + e.getMessage() + "\n\n" +
                            "Si el mensaje habla de columnas faltantes " +
                            "(medio_pago / observaciones), cierra la aplicacion " +
                            "y vuelvela a abrir: la migracion automatica las creara."
            );
            err.showAndWait();
        }
    }

    private void mostrarResumenTiquete(String trabajador,
                                       String fecha, double total, int jornadas) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tiquete generado");
        alert.setHeaderText("🧾 Pago registrado exitosamente");
        alert.setContentText(
                "Trabajador:  " + trabajador + "\n" +
                        "Fecha pago:  " + fecha + "\n" +
                        "Jornadas:    " + jornadas + "\n" +
                        "─────────────────────────\n" +
                        "TOTAL:  $" + String.format("%,.0f", total)
        );
        alert.showAndWait();
    }

    private void verTiquete(ObservableList<String> fila) {
        try {
            // Obtener datos extra de la BD
            String[] datosExtra = obtenerDatosExtra(fila.get(0));

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass().getResource("/fxml/detalle_tiquete.fxml")
                    );
            javafx.scene.Parent root = loader.load();

            DetalleTiqueteController ctrl = loader.getController();
            ctrl.cargarDatos(
                    fila.get(0),   // numero
                    fila.get(1),   // trabajador
                    fila.get(2),   // fecha pago
                    fila.get(3),   // labor
                    fila.get(4),   // lote
                    datosExtra[0], // modo pago (kilo/dia)
                    datosExtra[1], // kilos
                    fila.get(5),   // total
                    fila.get(6),   // estado
                    datosExtra[2], // medio de pago (efectivo/nequi...)
                    datosExtra[3], // observaciones
                    datosExtra[4]  // cedula
            );

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("🧾 Tiquete " + fila.get(0));
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.out.println("❌ Error ver tiquete: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] obtenerDatosExtra(String numero) {
        // Extraer número limpio (quitar "N°")
        String numLimpio = numero.replace("N°", "").trim();
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT j.modo_pago, j.kilos, t.medio_pago, " +
                            "t.observaciones, tr.cedula " +
                            "FROM tiquetes t " +
                            "JOIN jornadas j ON t.jornada_id = j.id " +
                            "JOIN trabajadores tr ON t.trabajador_id = tr.id " +
                            "WHERE t.numero_tiquete = ?"
            );
            ps.setInt(1, Integer.parseInt(numLimpio));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String medio = rs.getString("medio_pago");
                String obs   = rs.getString("observaciones");
                String ced   = rs.getString("cedula");
                return new String[]{
                        rs.getString("modo_pago"),
                        String.valueOf(rs.getDouble("kilos")),
                        medio != null ? medio : "",
                        obs != null ? obs : "",
                        ced != null ? ced : ""
                };
            }
        } catch (Exception e) {
            System.out.println("❌ Error datos extra: " + e.getMessage());
        }
        return new String[]{"dia", "0.0", "", "", ""};
    }

    @FXML
    private void cancelar() {
        panelPago.setVisible(false);
        panelPago.setManaged(false);
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
package com.nufi.ui.controllers;

import com.nufi.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class JornadasController {

    @FXML private TableView<Jornada>            tablaJornadas;
    @FXML private TableColumn<Jornada, Integer> colId;
    @FXML private TableColumn<Jornada, String>  colTrabajador;
    @FXML private TableColumn<Jornada, String>  colLote;
    @FXML private TableColumn<Jornada, String>  colFecha;
    @FXML private TableColumn<Jornada, String>  colTipo;
    @FXML private TableColumn<Jornada, String>  colPago;
    @FXML private TableColumn<Jornada, Double>  colKilos;
    @FXML private TableColumn<Jornada, Double>  colTotal;
    @FXML private TableColumn<Jornada, Void>    colAcciones;

    @FXML private VBox        panelFormulario;
    @FXML private ComboBox<String> cmbTrabajador;
    @FXML private ComboBox<String> cmbLote;
    @FXML private DatePicker   dpFecha;
    @FXML private ComboBox<String> cmbTipoTrabajo;
    @FXML private ComboBox<String> cmbModoPago;
    @FXML private HBox         panelKilos;
    @FXML private HBox         panelDia;
    @FXML private TextField    txtKilos;
    @FXML private TextField    txtValorKilo;
    @FXML private TextField    txtValorDia;
    @FXML private Label        lblTotal;
    @FXML private TextField    txtObservaciones;
    @FXML private Label        lblError;
    @FXML private TextField txtKilosDia;

    private final BaseDatos db = ConexionDB.getInstance();
    private List<Trabajador> listaTrabajadores;
    private List<Lote>       listaLotes;

    @FXML
    public void initialize() {
        tablaJornadas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas();
        cargarCombos();
        cargarJornadas();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colTrabajador.setCellValueFactory(
                new PropertyValueFactory<>("nombreTrabajador"));
        colLote.setCellValueFactory(
                new PropertyValueFactory<>("nombreLote"));
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipoTrabajo"));
        colPago.setCellValueFactory(
                new PropertyValueFactory<>("modoPago"));
        colKilos.setCellValueFactory(
                new PropertyValueFactory<>("kilos"));
        colTotal.setCellValueFactory(
                new PropertyValueFactory<>("totalPagar"));

        // ✅ Centrar texto en todas las columnas
        centrarColumna(colId);
        centrarColumna(colTrabajador);
        centrarColumna(colLote);
        centrarColumna(colFecha);
        centrarColumna(colTipo);
        centrarColumna(colPago);
        centrarColumna(colKilos);
        centrarColumna(colTotal);

        // ✅ Kilos sin decimales
        colKilos.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item.intValue()));
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setStyle("-fx-padding: 0 0 0 12;");
                }
            }
        });

        // ✅ Total con formato colombiano $72.000
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%,.0f", item)
                            .replace(",", "."));
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setStyle("-fx-padding: 0 0 0 12;");
                }
            }
        });

        // Columna acciones — ya está bien
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁 Detalle");
            private final HBox box = new HBox(btnVer);
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnVer.setStyle(
                        "-fx-background-color:#457b9d;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:4 10;" +
                                "-fx-cursor:hand;");
                btnVer.setOnAction(e -> {
                    Jornada j = getTableView()
                            .getItems().get(getIndex());
                    verDetalleJornada(j);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Listeners modo pago
        cmbModoPago.valueProperty().addListener((obs, old, nuevo) -> {
            if ("Kilo".equals(nuevo)) {
                panelKilos.setVisible(true);
                panelKilos.setManaged(true);
                panelDia.setVisible(false);
                panelDia.setManaged(false);
            } else if ("Día".equals(nuevo)) {
                panelKilos.setVisible(false);
                panelKilos.setManaged(false);
                panelDia.setVisible(true);
                panelDia.setManaged(true);
            }
            calcularTotal();
        });

        txtKilos.textProperty().addListener((obs, old, n) -> calcularTotal());
        txtValorKilo.textProperty().addListener((obs, old, n) -> calcularTotal());
        txtValorDia.textProperty().addListener((obs, old, n) -> calcularTotal());
    }

    private void verDetalleJornada(Jornada j) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle jornada");
        alert.setHeaderText("📅 Jornada #" + j.id);
        alert.setContentText(
                "Trabajador:  " + j.nombreTrabajador + "\n" +
                        "Lote:        " + j.nombreLote + "\n" +
                        "Fecha:       " + j.fecha + "\n" +
                        "Labor:       " + j.tipoTrabajo + "\n" +
                        "Modo pago:   " + j.modoPago + "\n" +
                        "Kilos:       " + j.kilos + " kg\n" +
                        "─────────────────────────\n" +
                        "TOTAL:  $" + String.format("%,.0f", j.totalPagar)
        );
        alert.showAndWait();
    }

    // ✅ Método helper igual que en Lotes y Trabajadores
    private <T> void centrarColumna(TableColumn<Jornada, T> col) {
        col.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    setStyle("-fx-padding: 0 0 0 12;");
                }
            }
        });
    }

    private void cargarCombos() {
        // Trabajadores
        listaTrabajadores = db.obtenerTrabajadores();
        ObservableList<String> nombresTrab =
                FXCollections.observableArrayList();
        for (Trabajador t : listaTrabajadores) {
            nombresTrab.add(t.nombre);
        }
        cmbTrabajador.setItems(nombresTrab);

        // Lotes
        listaLotes = db.obtenerLotesConKilos();
        ObservableList<String> nombresLotes =
                FXCollections.observableArrayList();
        for (Lote l : listaLotes) {
            nombresLotes.add(l.nombre);
        }
        cmbLote.setItems(nombresLotes);

        // Tipos de trabajo
        cmbTipoTrabajo.setItems(FXCollections.observableArrayList(
                "Recolección", "Abono", "Siembra", "Guadana"
        ));

        // Modo pago
        cmbModoPago.setItems(FXCollections.observableArrayList(
                "Kilo", "Día"
        ));
    }

    private void calcularTotal() {
        try {
            double total = 0;
            String modo = cmbModoPago.getValue();
            if ("kilo".equals(modo)) {
                double kilos = Double.parseDouble(
                        txtKilos.getText().trim());
                double valorKilo = Double.parseDouble(
                        txtValorKilo.getText().trim());
                total = kilos * valorKilo;
            } else if ("dia".equals(modo)) {
                total = Double.parseDouble(
                        txtValorDia.getText().trim());
            }
            lblTotal.setText(String.format("$%,.0f", total));
        } catch (Exception e) {
            lblTotal.setText("$0");
        }
    }

    private void cargarJornadas() {
        ObservableList<Jornada> lista =
                FXCollections.observableArrayList(
                        db.obtenerJornadas()
                );
        tablaJornadas.setItems(lista);
    }

    @FXML
    private void abrirFormularioNuevo() {
        cmbTrabajador.setValue(null);
        cmbLote.setValue(null);
        dpFecha.setValue(LocalDate.now());
        cmbTipoTrabajo.setValue(null);
        cmbModoPago.setValue(null);
        txtKilos.clear();
        txtValorKilo.clear();
        txtValorDia.clear();
        txtKilosDia.clear();
        txtObservaciones.clear();
        lblTotal.setText("$0");
        ocultarError();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void guardarJornada() {

        // Validaciones básicas primero
        if (cmbTrabajador.getValue() == null ||
                cmbLote.getValue() == null ||
                dpFecha.getValue() == null ||
                cmbTipoTrabajo.getValue() == null ||
                cmbModoPago.getValue() == null) {
            mostrarError("Completa todos los campos obligatorios.");
            return;
        }

        // Declarar tipo ANTES de usarlo
        String tipo = cmbTipoTrabajo.getValue();
        String modo = cmbModoPago.getValue();

        // ✅ Validar cosecha activa si es recolección
        if ("Recolección".equals(tipo)) {
            int cosechaId = db.obtenerCosechaActivaId();
            if (cosechaId == -1) {
                mostrarError(
                        "No hay cosecha activa. Inicia una cosecha " +
                                "antes de registrar recolección.");
                return;
            }
        }

        // Obtener IDs
        int trabajadorId = listaTrabajadores
                .get(cmbTrabajador.getSelectionModel()
                        .getSelectedIndex()).id;
        int loteId = listaLotes
                .get(cmbLote.getSelectionModel()
                        .getSelectedIndex()).id;

        String fecha = dpFecha.getValue().toString();
        String obs   = txtObservaciones.getText().trim();

        double kilos = 0, valorDia = 0, valorKilo = 0;
        try {
            if ("Kilo".equals(modo)) {
                kilos     = Double.parseDouble(txtKilos.getText());
                valorKilo = Double.parseDouble(txtValorKilo.getText());
            } else {
                valorDia = Double.parseDouble(txtValorDia.getText());
                String kilosDiaStr = txtKilosDia.getText().trim();
                if (!kilosDiaStr.isEmpty()) {
                    kilos = Double.parseDouble(kilosDiaStr);
                }
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingresa valores numéricos válidos.");
            return;
        }

        Jornada j = new Jornada(
                trabajadorId, loteId, fecha,
                tipo, modo, kilos, valorDia, valorKilo, obs
        );
        db.guardarJornada(j);

        // ✅ Si es recolección → actualizar cosecha
        if ("Recolección".equals(tipo) && kilos > 0) {
            int cosechaId = db.obtenerCosechaActivaId();
            if (cosechaId != -1) {
                int cosechaLoteId = db.obtenerCosechaLoteId(
                        cosechaId, loteId);
                if (cosechaLoteId != -1) {
                    db.actualizarCerezaLote(cosechaLoteId, kilos);
                } else {
                    CosechaLote cl = new CosechaLote(
                            cosechaId, loteId, fecha);
                    cl.observaciones = "Agregado automáticamente";
                    db.guardarCosechaLote(cl);
                    int nuevoId = db.obtenerCosechaLoteId(
                            cosechaId, loteId);
                    if (nuevoId != -1) {
                        db.actualizarCerezaLote(nuevoId, kilos);
                    }
                }
                mostrarAlertaCosecha(kilos, loteId);
            }
        }

        cancelar();
        cargarJornadas();
    }

    private void mostrarAlertaCosecha(double kilos, int loteId) {
        String nombreLote = listaLotes.stream()
                .filter(l -> l.id == loteId)
                .findFirst()
                .map(l -> l.nombre)
                .orElse("Lote");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Cosecha actualizada");
        alert.setHeaderText("☕ Kilos registrados en cosecha");
        alert.setContentText(
                "✅ Se agregaron " + kilos + " kg de cereza\n" +
                        "al lote " + nombreLote + " en la cosecha activa."
        );
        alert.showAndWait();
    }
    private String estadoEmoji(String estado) {
        return switch (estado) {
            case "en_proceso" -> "● En proceso";
            case "en_espera"  -> "|| En espera";
            case "terminado"  -> "✓ Terminada";
            case "pendiente"  -> "✗ Pendiente";
            default           -> estado;
        };
    }

    @FXML
    private void cancelar() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
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
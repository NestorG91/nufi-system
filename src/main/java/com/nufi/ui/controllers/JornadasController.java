package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Jornada;
import com.nufi.Lote;
import com.nufi.Trabajador;
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

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer = new Button("👁 Ver");
            private final HBox box = new HBox(btnVer);
            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnVer.setStyle(
                        "-fx-background-color:#2d6a4f;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:4 10;" +
                                "-fx-cursor:hand;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Listener modo pago
        cmbModoPago.valueProperty().addListener((obs, old, nuevo) -> {
            if ("kilo".equals(nuevo)) {
                panelKilos.setVisible(true);
                panelKilos.setManaged(true);
                panelDia.setVisible(false);
                panelDia.setManaged(false);
            } else if ("dia".equals(nuevo)) {
                panelKilos.setVisible(false);
                panelKilos.setManaged(false);
                panelDia.setVisible(true);
                panelDia.setManaged(true);
            }
            calcularTotal();
        });

        // Listeners para calcular total
        txtKilos.textProperty().addListener(
                (obs, old, n) -> calcularTotal());
        txtValorKilo.textProperty().addListener(
                (obs, old, n) -> calcularTotal());
        txtValorDia.textProperty().addListener(
                (obs, old, n) -> calcularTotal());
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
                "recoleccion", "abono", "siembra", "guadana"
        ));

        // Modo pago
        cmbModoPago.setItems(FXCollections.observableArrayList(
                "kilo", "dia"
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
        txtObservaciones.clear();
        lblTotal.setText("$0");
        ocultarError();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void guardarJornada() {
        // Validaciones
        if (cmbTrabajador.getValue() == null ||
                cmbLote.getValue() == null ||
                dpFecha.getValue() == null ||
                cmbTipoTrabajo.getValue() == null ||
                cmbModoPago.getValue() == null) {
            mostrarError("Completa todos los campos obligatorios.");
            return;
        }

        // Obtener IDs
        int trabajadorId = listaTrabajadores
                .get(cmbTrabajador.getSelectionModel()
                        .getSelectedIndex()).id;
        int loteId = listaLotes
                .get(cmbLote.getSelectionModel()
                        .getSelectedIndex()).id;

        String fecha    = dpFecha.getValue().toString();
        String tipo     = cmbTipoTrabajo.getValue();
        String modo     = cmbModoPago.getValue();
        String obs      = txtObservaciones.getText().trim();

        double kilos = 0, valorDia = 0, valorKilo = 0;

        try {
            if ("kilo".equals(modo)) {
                kilos     = Double.parseDouble(txtKilos.getText());
                valorKilo = Double.parseDouble(txtValorKilo.getText());
            } else {
                valorDia = Double.parseDouble(txtValorDia.getText());
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
        cancelar();
        cargarJornadas();
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
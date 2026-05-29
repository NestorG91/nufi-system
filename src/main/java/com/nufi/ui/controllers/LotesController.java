package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Lote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LotesController {

    @FXML private TableView<Lote>            tablaLotes;
    @FXML private TableColumn<Lote, Integer> colId;
    @FXML private TableColumn<Lote, String>  colNombre;
    @FXML private TableColumn<Lote, Integer> colMatas;
    @FXML private TableColumn<Lote, String>  colFecha;
    @FXML private TableColumn<Lote, Double>  colKilos;
    @FXML private TableColumn<Lote, Void>    colAcciones;

    @FXML private VBox      panelFormulario;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMatas;
    @FXML private TextField txtFecha;
    @FXML private Label     lblErrorLote;

    private final BaseDatos db = ConexionDB.getInstance();
    private Lote loteEditando = null;

    @FXML
    public void initialize() {
        tablaLotes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas();
        cargarLotes();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colMatas.setCellValueFactory(
                new PropertyValueFactory<>("matas"));
        colFecha.setCellValueFactory(
                new PropertyValueFactory<>("fechaSiembra"));
        colKilos.setCellValueFactory(
                new PropertyValueFactory<>("kilosCosechados"));

        centrarColumna(colId);
        centrarColumna(colNombre);
        centrarColumna(colMatas);
        centrarColumna(colFecha);
        centrarColumna(colKilos);

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️ Editar");
            private final HBox box = new HBox(btnEditar);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                btnEditar.setStyle(
                        "-fx-background-color:#2d6a4f;" +
                                "-fx-text-fill:white;" +
                                "-fx-background-radius:6;" +
                                "-fx-padding:4 10;" +
                                "-fx-cursor:hand;");
                btnEditar.setOnAction(e -> {
                    Lote lote = getTableView()
                            .getItems().get(getIndex());
                    abrirFormularioEditar(lote);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private <T> void centrarColumna(TableColumn<Lote, T> col) {
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

    private void cargarLotes() {
        ObservableList<Lote> lista =
                FXCollections.observableArrayList(
                        db.obtenerLotesConKilos()
                );
        tablaLotes.setItems(lista);
    }

    @FXML
    private void abrirFormularioNuevo() {
        loteEditando = null;
        txtNombre.clear();
        txtMatas.clear();
        txtFecha.clear();
        ocultarError();
        mostrarFormulario();
    }

    private void abrirFormularioEditar(Lote lote) {
        loteEditando = lote;
        txtNombre.setText(lote.nombre);
        txtMatas.setText(String.valueOf(lote.matas));
        txtFecha.setText(lote.fechaSiembra);
        ocultarError();
        mostrarFormulario();
    }

    @FXML
    private void guardarLote() {
        String nombre   = txtNombre.getText().trim();
        String matasStr = txtMatas.getText().trim();
        String fecha    = txtFecha.getText().trim();

        if (nombre.isEmpty() || matasStr.isEmpty() || fecha.isEmpty()) {
            mostrarError("Completa todos los campos.");
            return;
        }

        int matas;
        try {
            matas = Integer.parseInt(matasStr);
        } catch (NumberFormatException e) {
            mostrarError("Las matas deben ser un número.");
            return;
        }

        if (loteEditando != null) {
            // ✅ Actualizar lote existente
            db.actualizarLote(loteEditando.id, nombre, matas, fecha);
        } else {
            // ✅ Crear lote nuevo
            db.guardarLote(new Lote(nombre, matas, fecha));
        }

        cancelar();
        cargarLotes();
    }

    @FXML
    private void cancelar() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        loteEditando = null;
    }

    private void mostrarFormulario() {
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void mostrarError(String msg) {
        lblErrorLote.setText(msg);
        lblErrorLote.setVisible(true);
        lblErrorLote.setManaged(true);
    }

    private void ocultarError() {
        lblErrorLote.setVisible(false);
        lblErrorLote.setManaged(false);
    }
}
package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Trabajador;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TrabajadoresController {

    @FXML private TableView<Trabajador>            tablaTrabajadores;
    @FXML private TableColumn<Trabajador, Integer> colId;
    @FXML private TableColumn<Trabajador, String>  colNombre;
    @FXML private TableColumn<Trabajador, String>  colCedula;
    @FXML private TableColumn<Trabajador, String>  colTelefono;
    @FXML private TableColumn<Trabajador, String>  colDireccion;
    @FXML private TableColumn<Trabajador, Void>    colAcciones;

    @FXML private VBox      panelFormulario;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCedula;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private Label     lblError;

    private final BaseDatos db = ConexionDB.getInstance();
    private Trabajador trabajadorEditando = null;

    @FXML
    public void initialize() {
        tablaTrabajadores.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas();
        cargarTrabajadores();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colCedula.setCellValueFactory(
                new PropertyValueFactory<>("cedula"));
        colTelefono.setCellValueFactory(
                new PropertyValueFactory<>("telefono"));
        colDireccion.setCellValueFactory(
                new PropertyValueFactory<>("direccion"));

        centrarColumna(colId);
        centrarColumna(colNombre);
        centrarColumna(colCedula);
        centrarColumna(colTelefono);
        centrarColumna(colDireccion);

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
                    Trabajador t = getTableView()
                            .getItems().get(getIndex());
                    abrirFormularioEditar(t);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private <T> void centrarColumna(TableColumn<Trabajador, T> col) {
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

    private void cargarTrabajadores() {
        ObservableList<Trabajador> lista =
                FXCollections.observableArrayList(
                        db.obtenerTrabajadores()
                );
        tablaTrabajadores.setItems(lista);
    }

    @FXML
    private void abrirFormularioNuevo() {
        trabajadorEditando = null;
        txtNombre.clear();
        txtCedula.clear();
        txtTelefono.clear();
        txtDireccion.clear();
        ocultarError();
        mostrarFormulario();
    }

    private void abrirFormularioEditar(Trabajador t) {
        trabajadorEditando = t;
        txtNombre.setText(t.nombre);
        txtCedula.setText(t.cedula);
        txtTelefono.setText(t.telefono);
        txtDireccion.setText(t.direccion);
        ocultarError();
        mostrarFormulario();
    }

    @FXML
    private void guardarTrabajador() {
        String nombre    = txtNombre.getText().trim();
        String cedula    = txtCedula.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty() || cedula.isEmpty()) {
            mostrarError("Nombre y cédula son obligatorios.");
            return;
        }

        if (trabajadorEditando != null) {
            db.actualizarTrabajador(
                    trabajadorEditando.id,
                    nombre, cedula, telefono, direccion
            );
        } else {
            db.guardarTrabajador(
                    new Trabajador(nombre, cedula, telefono, direccion)
            );
        }

        cancelar();
        cargarTrabajadores();
    }

    @FXML
    private void cancelar() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        trabajadorEditando = null;
    }

    private void mostrarFormulario() {
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
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
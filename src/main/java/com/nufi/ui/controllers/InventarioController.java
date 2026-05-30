package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InventarioController {

    @FXML private TableView<Producto>            tablaInventario;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String>  colNombre;
    @FXML private TableColumn<Producto, String>  colTipo;
    @FXML private TableColumn<Producto, String>  colUnidad;
    @FXML private TableColumn<Producto, Double>  colStock;
    @FXML private TableColumn<Producto, Double>  colMinimo;
    @FXML private TableColumn<Producto, Double>  colPrecio;
    @FXML private TableColumn<Producto, String>  colEstado;
    @FXML private TableColumn<Producto, Void>    colAcciones;

    @FXML private VBox      panelFormulario;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextField txtStock;
    @FXML private TextField txtMinimo;
    @FXML private TextField txtPrecio;
    @FXML private Label     lblError;

    private final BaseDatos db = ConexionDB.getInstance();
    private Producto productoEditando = null;

    @FXML
    public void initialize() {
        tablaInventario.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarCombos();
        configurarColumnas();
        cargarProductos();
    }

    private void configurarCombos() {
        cmbTipo.setItems(FXCollections.observableArrayList(
                "abono", "veneno", "herramienta", "fertilizante", "otro"
        ));
        cmbUnidad.setItems(FXCollections.observableArrayList(
                "bultos", "litros", "kilos", "unidad", "metros"
        ));
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(
                new PropertyValueFactory<>("tipo"));
        colUnidad.setCellValueFactory(
                new PropertyValueFactory<>("unidadMedida"));
        colStock.setCellValueFactory(
                new PropertyValueFactory<>("stockActual"));
        colMinimo.setCellValueFactory(
                new PropertyValueFactory<>("stockMinimo"));
        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precioUnidad"));

        // Columna estado con alerta
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null ||
                        getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                Producto p = (Producto) getTableRow().getItem();
                if (p.stockActual <= p.stockMinimo) {
                    setText("⚠️ Stock bajo");
                    setStyle("-fx-text-fill:#c1121f;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                } else {
                    setText("✅ OK");
                    setStyle("-fx-text-fill:#2d6a4f;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                }
            }
        });

        centrarColumna(colId);
        centrarColumna(colNombre);
        centrarColumna(colTipo);
        centrarColumna(colUnidad);
        centrarColumna(colStock);
        centrarColumna(colMinimo);
        centrarColumna(colPrecio);

        // Columna acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar  = new Button("✏️ Editar");
            private final Button btnEntrada = new Button("➕");
            private final Button btnSalida  = new Button("➖");
            private final HBox box = new HBox(4, btnEditar, btnEntrada, btnSalida);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                String estilo = "-fx-background-radius:6;" +
                        "-fx-padding:4 8;" +
                        "-fx-cursor:hand;" +
                        "-fx-text-fill:white;";
                btnEditar.setStyle(estilo +
                        "-fx-background-color:#2d6a4f;");
                btnEntrada.setStyle(estilo +
                        "-fx-background-color:#40916c;");
                btnSalida.setStyle(estilo +
                        "-fx-background-color:#c1121f;");

                btnEditar.setOnAction(e -> {
                    Producto p = getTableView()
                            .getItems().get(getIndex());
                    abrirFormularioEditar(p);
                });
                btnEntrada.setOnAction(e -> {
                    Producto p = getTableView()
                            .getItems().get(getIndex());
                    registrarMovimiento(p, "entrada");
                });
                btnSalida.setOnAction(e -> {
                    Producto p = getTableView()
                            .getItems().get(getIndex());
                    registrarMovimiento(p, "salida");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private <T> void centrarColumna(TableColumn<Producto, T> col) {
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

    private void cargarProductos() {
        ObservableList<Producto> lista =
                FXCollections.observableArrayList(
                        db.obtenerProductos()
                );
        tablaInventario.setItems(lista);
    }

    @FXML
    private void abrirFormularioNuevo() {
        productoEditando = null;
        txtNombre.clear();
        cmbTipo.setValue(null);
        cmbUnidad.setValue(null);
        txtStock.clear();
        txtMinimo.clear();
        txtPrecio.clear();
        ocultarError();
        mostrarFormulario();
    }

    private void abrirFormularioEditar(Producto p) {
        productoEditando = p;
        txtNombre.setText(p.nombre);
        cmbTipo.setValue(p.tipo);
        cmbUnidad.setValue(p.unidadMedida);
        txtStock.setText(String.valueOf(p.stockActual));
        txtMinimo.setText(String.valueOf(p.stockMinimo));
        txtPrecio.setText(String.valueOf(p.precioUnidad));
        ocultarError();
        mostrarFormulario();
    }

    private void registrarMovimiento(Producto p, String tipo) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle(tipo.equals("entrada") ? "➕ Entrada de stock" : "➖ Salida de stock");
        dialog.setHeaderText("Producto: " + p.nombre);
        dialog.setContentText("Cantidad a " + tipo + ":");

        dialog.showAndWait().ifPresent(cantStr -> {
            try {
                double cantidad = Double.parseDouble(cantStr.trim());
                String fecha = java.time.LocalDate.now().toString();

                if (tipo.equals("salida")) {
                    // ✅ Preguntar si se usa en un lote
                    preguntarLoteParaSalida(p, cantidad, fecha);
                } else {
                    // Entrada directa sin lote
                    db.registrarMovimientoSinLote(
                            p.id, tipo, cantidad, fecha, "Entrada manual"
                    );
                    cargarProductos();
                }

            } catch (NumberFormatException ex) {
                mostrarError("Ingresa un número válido.");
            }
        });
    }

    private void preguntarLoteParaSalida(Producto p, double cantidad, String fecha) {

        // Construir opciones de lotes
        java.util.List<com.nufi.Lote> lotes = db.obtenerLotesConKilos();

        ChoiceDialog<String> dialogLote = new ChoiceDialog<>(
                "Sin lote específico",
                buildOpcionesLote(lotes)
        );
        dialogLote.setTitle("¿En qué lote se usará?");
        dialogLote.setHeaderText("Producto: " + p.nombre +
                " | Cantidad: " + cantidad);
        dialogLote.setContentText("Selecciona el lote:");

        dialogLote.showAndWait().ifPresent(opcion -> {
            int loteId = 0;
            String obs = "Salida manual";

            // Buscar el lote seleccionado
            for (com.nufi.Lote lote : lotes) {
                if (opcion.startsWith(lote.nombre)) {
                    loteId = lote.id;
                    obs = "Usado en lote: " + lote.nombre;
                    break;
                }
            }

            if (loteId > 0) {
                db.registrarMovimiento(
                        p.id, loteId, "salida", cantidad, fecha, obs
                );
            } else {
                db.registrarMovimientoSinLote(
                        p.id, "salida", cantidad, fecha, "Salida sin lote"
                );
            }
            cargarProductos();
        });
    }

    private java.util.List<String> buildOpcionesLote(
            java.util.List<com.nufi.Lote> lotes) {
        java.util.List<String> opciones = new java.util.ArrayList<>();
        opciones.add("Sin lote específico");
        for (com.nufi.Lote lote : lotes) {
            opciones.add(lote.nombre + " (" + lote.matas + " matas)");
        }
        return opciones;
    }

    @FXML
    private void guardarProducto() {
        String nombre = txtNombre.getText().trim();
        String tipo   = cmbTipo.getValue();
        String unidad = cmbUnidad.getValue();
        String stockStr  = txtStock.getText().trim();
        String minimoStr = txtMinimo.getText().trim();
        String precioStr = txtPrecio.getText().trim();

        if (nombre.isEmpty() || tipo == null || unidad == null ||
                stockStr.isEmpty() || minimoStr.isEmpty() || precioStr.isEmpty()) {
            mostrarError("Completa todos los campos.");
            return;
        }

        double stock, minimo, precio;
        try {
            stock  = Double.parseDouble(stockStr);
            minimo = Double.parseDouble(minimoStr);
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            mostrarError("Stock, mínimo y precio deben ser números.");
            return;
        }

        if (productoEditando != null) {
            db.actualizarProducto(
                    productoEditando.id,
                    nombre, tipo, unidad, stock, minimo, precio
            );
        } else {
            db.guardarProducto(
                    new Producto(nombre, tipo, unidad, stock, minimo, precio)
            );
        }

        cancelar();
        cargarProductos();
    }

    @FXML
    private void cancelar() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        productoEditando = null;
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
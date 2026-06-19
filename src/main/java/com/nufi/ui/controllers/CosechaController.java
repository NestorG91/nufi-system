package com.nufi.ui.controllers;

import com.nufi.*;
import com.nufi.ui.LoadingUtil;
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

public class CosechaController {

    @FXML private TableView<ObservableList<String>> tablaCosechas;
    @FXML private TableColumn<ObservableList<String>, String> colId;
    @FXML private TableColumn<ObservableList<String>, String> colNombre;
    @FXML private TableColumn<ObservableList<String>, String> colEstado;
    @FXML private TableColumn<ObservableList<String>, String> colFechaInicio;
    @FXML private TableColumn<ObservableList<String>, String> colFechaFin;
    @FXML private TableColumn<ObservableList<String>, String> colCereza;
    @FXML private TableColumn<ObservableList<String>, String> colLotes;
    @FXML private TableColumn<ObservableList<String>, String> colPergaminoProm;
    @FXML private TableColumn<ObservableList<String>, String> colPergaminoReal;
    @FXML private TableColumn<ObservableList<String>, Void>   colAcciones;

    @FXML private VBox       panelFormulario;
    @FXML private TextField  txtNombre;
    @FXML private DatePicker dpFechaInicio;
    @FXML private TextField  txtObservaciones;
    @FXML private VBox       panelLotes;
    @FXML private Label      lblError;

    private final BaseDatos      db            = ConexionDB.getInstance();
    private final List<CheckBox> checkboxLotes = new ArrayList<>();
    private List<Lote>           listaLotes;

    @FXML
    public void initialize() {
        tablaCosechas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        configurarColumnas();
        cargarCosechas();
    }

    private void configurarColumnas() {
        // ✅ Índices en orden exacto con cargarCosechas()
        colId.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(0)));
        colNombre.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(1)));
        colEstado.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(2)));
        colFechaInicio.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(3)));
        colFechaFin.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(4)));
        colCereza.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(5)));
        colLotes.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(6)));
        colPergaminoProm.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(7)));
        colPergaminoReal.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().get(8)));

        // Color en columna estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                if (item.contains("En proceso")) {
                    setStyle("-fx-text-fill:#e9a800;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                } else if (item.contains("En espera")) {
                    setStyle("-fx-text-fill:#457b9d;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                } else if (item.contains("Terminada")) {
                    setStyle("-fx-text-fill:#2d6a4f;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                } else {
                    setStyle("-fx-text-fill:#c1121f;" +
                            "-fx-font-weight:bold;" +
                            "-fx-padding:0 0 0 12;");
                }
            }
        });

        // Columna acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetalle   = new Button("📋 Detalle");
            private final Button btnRegistrar = new Button("☕ Kg");
            private final Button btnCerrar    = new Button("✅ Cerrar");
            private final HBox box = new HBox(4,
                    btnDetalle, btnRegistrar, btnCerrar);

            {
                box.setAlignment(javafx.geometry.Pos.CENTER);
                String estilo = "-fx-background-radius:6;" +
                        "-fx-padding:4 6;" +
                        "-fx-cursor:hand;" +
                        "-fx-text-fill:white;" +
                        "-fx-font-size:11px;";
                btnDetalle.setStyle(estilo +
                        "-fx-background-color:#457b9d;");
                btnRegistrar.setStyle(estilo +
                        "-fx-background-color:#2d6a4f;");
                btnCerrar.setStyle(estilo +
                        "-fx-background-color:#6c757d;");

                btnDetalle.setOnAction(e -> {
                    ObservableList<String> fila = getTableView()
                            .getItems().get(getIndex());
                    verDetalle(Integer.parseInt(fila.get(0)),
                            fila.get(1));
                });
                btnRegistrar.setOnAction(e -> {
                    ObservableList<String> fila = getTableView()
                            .getItems().get(getIndex());
                    registrarKilos(Integer.parseInt(fila.get(0)),
                            fila.get(1));
                });
                btnCerrar.setOnAction(e -> {
                    ObservableList<String> fila = getTableView()
                            .getItems().get(getIndex());
                    cerrarCosecha(Integer.parseInt(fila.get(0)),
                            fila.get(1));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void cargarCosechas() {
        ObservableList<ObservableList<String>> datos =
                FXCollections.observableArrayList();
        try {
            ResultSet rs = db.getConexion()
                    .createStatement().executeQuery(
                            "SELECT c.id, c.nombre, c.estado, " +
                                    "c.fecha_inicio, c.fecha_fin, " +
                                    "COALESCE(SUM(cl.total_cereza_kg),0) as cereza, " +
                                    "COALESCE(SUM(cl.estimado_pergamino_kg),0) as perg_est, " +
                                    "COALESCE(SUM(cl.pergamino_real_kg),0) as perg_real " +
                                    "FROM cosecha c " +
                                    "LEFT JOIN cosecha_lotes cl ON c.id=cl.cosecha_id " +
                                    "GROUP BY c.id ORDER BY c.fecha_inicio DESC"
                    );
            while (rs.next()) {
                int cosechaId = rs.getInt("id");
                String lotes  = obtenerNombresLotes(cosechaId);

                ObservableList<String> fila =
                        FXCollections.observableArrayList();
                fila.add(String.valueOf(cosechaId));            // 0 colId
                fila.add(rs.getString("nombre"));               // 1 colNombre
                fila.add(estadoEmoji(rs.getString("estado")));  // 2 colEstado
                fila.add(rs.getString("fecha_inicio") != null ? // 3 colFechaInicio
                        rs.getString("fecha_inicio") : "—");
                fila.add(rs.getString("fecha_fin") != null ?    // 4 colFechaFin
                        rs.getString("fecha_fin") : "En curso");
                fila.add(String.format("%.1f",                  // 5 colCereza
                        rs.getDouble("cereza")) + " kg");
                fila.add(lotes);                                // 6 colLotes
                fila.add(String.format("%.1f",                  // 7 colPergaminoProm
                        rs.getDouble("perg_est")) + " kg");
                fila.add(String.format("%.1f",                  // 8 colPergaminoReal
                        rs.getDouble("perg_real")) + " kg");
                datos.add(fila);
            }
        } catch (Exception e) {
            System.out.println("❌ Error cosechas: " + e.getMessage());
        }
        tablaCosechas.setItems(datos);
    }

    private String obtenerNombresLotes(int cosechaId) {
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT l.nombre FROM cosecha_lotes cl " +
                            "JOIN lotes l ON cl.lote_id=l.id " +
                            "WHERE cl.cosecha_id=?"
            );
            ps.setInt(1, cosechaId);
            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(rs.getString("nombre"));
            }
            return sb.length() > 0 ? sb.toString() : "—";
        } catch (Exception e) {
            return "—";
        }
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
    private void abrirFormularioNuevo() {
        txtNombre.clear();
        dpFechaInicio.setValue(LocalDate.now());
        txtObservaciones.clear();
        ocultarError();
        cargarCheckboxLotes();
        mostrarFormulario();
    }

    private void cargarCheckboxLotes() {
        panelLotes.getChildren().clear();
        checkboxLotes.clear();
        listaLotes = db.obtenerLotesConKilos();
        for (Lote lote : listaLotes) {
            CheckBox cb = new CheckBox(
                    lote.nombre + " — " + lote.matas + " matas"
            );
            cb.setStyle("-fx-font-size:13px;");
            checkboxLotes.add(cb);
            panelLotes.getChildren().add(cb);
        }
    }

    @FXML
    private void guardarCosecha() {
        String nombre    = txtNombre.getText().trim();
        LocalDate fecha  = dpFechaInicio.getValue();
        String obs       = txtObservaciones.getText().trim();

        if (nombre.isEmpty() || fecha == null) {
            mostrarError("Nombre y fecha son obligatorios.");
            return;
        }

        List<Lote> lotesSeleccionados = new ArrayList<>();
        for (int i = 0; i < checkboxLotes.size(); i++) {
            if (checkboxLotes.get(i).isSelected()) {
                lotesSeleccionados.add(listaLotes.get(i));
            }
        }

        if (lotesSeleccionados.isEmpty()) {
            mostrarError("Selecciona al menos un lote.");
            return;
        }

        Cosecha cosecha = new Cosecha(nombre, fecha.toString(), obs);
        int cosechaId   = db.guardarCosecha(cosecha);

        if (cosechaId == -1) {
            mostrarError("Error al crear la cosecha.");
            return;
        }

        for (Lote lote : lotesSeleccionados) {
            CosechaLote cl = new CosechaLote(
                    cosechaId, lote.id, fecha.toString()
            );
            cl.observaciones = "Lote agregado al inicio";
            db.guardarCosechaLote(cl);
        }

        cancelar();
        cargarCosechas();
    }

    private void verDetalle(int cosechaId, String nombre) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Detalle por lote:\n");
            sb.append("─────────────────────────────────\n");

            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT l.nombre, cl.estado, " +
                            "cl.total_cereza_kg, cl.estimado_pergamino_kg, " +
                            "cl.pergamino_real_kg " +
                            "FROM cosecha_lotes cl " +
                            "JOIN lotes l ON cl.lote_id=l.id " +
                            "WHERE cl.cosecha_id=?"
            );
            ps.setInt(1, cosechaId);
            ResultSet rs = ps.executeQuery();

            double totalCereza = 0, totalPergEst = 0, totalPergReal = 0;
            while (rs.next()) {
                String est = rs.getString("estado");
                String icono = est.equals("terminado") ? "✓" :
                        est.equals("en_espera") ? "||" : "●";
                sb.append(icono).append(" ")
                        .append(rs.getString("nombre")).append("\n")
                        .append("   Cereza: ")
                        .append(String.format("%.1f",
                                rs.getDouble("total_cereza_kg")))
                        .append(" kg | Perg. est.: ")
                        .append(String.format("%.1f",
                                rs.getDouble("estimado_pergamino_kg")))
                        .append(" kg | Perg. real: ")
                        .append(String.format("%.1f",
                                rs.getDouble("pergamino_real_kg")))
                        .append(" kg\n\n");
                totalCereza   += rs.getDouble("total_cereza_kg");
                totalPergEst  += rs.getDouble("estimado_pergamino_kg");
                totalPergReal += rs.getDouble("pergamino_real_kg");
            }

            sb.append("─────────────────────────────────\n");
            sb.append("Total cereza:       ")
                    .append(String.format("%.1f", totalCereza)).append(" kg\n");
            sb.append("Perg. estimado:     ")
                    .append(String.format("%.1f", totalPergEst)).append(" kg\n");
            sb.append("Perg. real:         ")
                    .append(String.format("%.1f", totalPergReal)).append(" kg");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detalle cosecha");
            alert.setHeaderText("☕ " + nombre);
            alert.setContentText(sb.toString());
            alert.showAndWait();

        } catch (Exception e) {
            System.out.println("❌ Error detalle: " + e.getMessage());
        }
    }

    private void registrarKilos(int cosechaId, String nombre) {
        try {
            // Obtener lotes activos
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT cl.id, l.nombre as lote, l.id as lote_id " +
                            "FROM cosecha_lotes cl " +
                            "JOIN lotes l ON cl.lote_id=l.id " +
                            "WHERE cl.cosecha_id=? AND cl.estado != 'terminado'"
            );
            ps.setInt(1, cosechaId);
            ResultSet rs = ps.executeQuery();

            List<String>  opciones  = new ArrayList<>();
            List<Integer> ids       = new ArrayList<>();
            List<Integer> loteIds   = new ArrayList<>();

            while (rs.next()) {
                opciones.add(rs.getString("lote"));
                ids.add(rs.getInt("id"));
                loteIds.add(rs.getInt("lote_id"));
            }

            if (opciones.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION,
                        "Todos los lotes están terminados.")
                        .showAndWait();
                return;
            }

            // Seleccionar lote
            ChoiceDialog<String> dialogLote = new ChoiceDialog<>(
                    opciones.get(0), opciones);
            dialogLote.setTitle("Registrar kilos");
            dialogLote.setHeaderText("Cosecha: " + nombre);
            dialogLote.setContentText("¿En qué lote?");

            dialogLote.showAndWait().ifPresent(loteElegido -> {
                int idx           = opciones.indexOf(loteElegido);
                int cosechaLoteId = ids.get(idx);
                int loteId        = loteIds.get(idx);

                // Ingresar kilos
                TextInputDialog dialogKilos = new TextInputDialog("0");
                dialogKilos.setTitle("Kilos de cereza");
                dialogKilos.setHeaderText("Lote: " + loteElegido);
                dialogKilos.setContentText("Kilos recogidos:");

                dialogKilos.showAndWait().ifPresent(kilosStr -> {
                    try {
                        double kilos = Double.parseDouble(kilosStr.trim());

                        // ✅ Seleccionar quién recogió
                        List<String> trabajadores = new ArrayList<>();
                        List<Integer> trabIds = new ArrayList<>();
                        try {
                            ResultSet rst = db.getConexion()
                                    .createStatement().executeQuery(
                                            "SELECT id, nombre FROM trabajadores " +
                                                    "ORDER BY nombre"
                                    );
                            while (rst.next()) {
                                trabajadores.add(rst.getString("nombre"));
                                trabIds.add(rst.getInt("id"));
                            }
                        } catch (Exception e) {
                            System.out.println("❌ " + e.getMessage());
                        }

                        String primero = trabajadores.isEmpty() ?
                                "" : trabajadores.get(0);
                        ChoiceDialog<String> dialogTrab =
                                new ChoiceDialog<>(primero, trabajadores);
                        dialogTrab.setTitle("¿Quién recogió?");
                        dialogTrab.setHeaderText(
                                loteElegido + " — " + kilos + " kg");
                        dialogTrab.setContentText("Trabajador:");

                        dialogTrab.showAndWait().ifPresent(trabElegido -> {
                            int trabId = trabIds.get(
                                    trabajadores.indexOf(trabElegido));

                            // Actualizar cereza en cosecha
                            db.actualizarCerezaLote(cosechaLoteId, kilos);

                            // ✅ Registrar jornada automáticamente
                            String fecha = java.time.LocalDate.now().toString();
                            Jornada j = new Jornada(
                                    trabId, loteId, fecha,
                                    "Recolección", "Día",
                                    kilos, 0, 0, "Registrado desde cosecha"
                            );
                            db.guardarJornada(j);

                            cargarCosechas();
                            new Alert(Alert.AlertType.INFORMATION,
                                    "✅ " + kilos + " kg registrados\n" +
                                            "Trabajador: " + trabElegido + "\n" +
                                            "Lote: " + loteElegido)
                                    .showAndWait();
                        });

                    } catch (NumberFormatException ex) {
                        new Alert(Alert.AlertType.ERROR,
                                "Ingresa un número válido.")
                                .showAndWait();
                    }
                });
            });

        } catch (Exception e) {
            System.out.println("❌ Error kilos: " + e.getMessage());
        }
    }

    private void cerrarCosecha(int cosechaId, String nombre) {
        String estadoActual = "";
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT estado FROM cosecha WHERE id=?");
            ps.setInt(1, cosechaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) estadoActual = rs.getString("estado");
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }

        // ✅ Cosecha TERMINADA → permitir reactivar
        if ("terminado".equals(estadoActual)) {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Esta cosecha está TERMINADA.\n" +
                            "¿Deseas REACTIVARLA y ponerla en proceso?",
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setTitle("Reactivar cosecha");
            confirm.setHeaderText("Cosecha: " + nombre);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    LoadingUtil.ejecutar(
                            tablaCosechas,
                            "Reactivando cosecha...",
                            () -> cambiarEstadoCosecha(cosechaId, "en_proceso"),
                            () -> {
                                new Alert(Alert.AlertType.INFORMATION,
                                        "✅ Cosecha reactivada — ahora está en proceso.")
                                        .showAndWait();
                                cargarCosechas();
                            }
                    );
                }
            });
            return;
        }

        // ✅ Cosecha EN ESPERA → reactivar o cerrar
        if ("en_espera".equals(estadoActual)) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(
                    "Reactivar cosecha",
                    "Reactivar cosecha",
                    "Cerrar definitivamente"
            );
            dialog.setTitle("Cosecha en espera");
            dialog.setHeaderText("Cosecha: " + nombre);
            dialog.setContentText("¿Qué deseas hacer?");

            dialog.showAndWait().ifPresent(opcion -> {
                if (opcion.equals("Reactivar cosecha")) {
                    LoadingUtil.ejecutar(
                            tablaCosechas,
                            "Reactivando cosecha...",
                            () -> cambiarEstadoCosecha(cosechaId, "en_proceso"),
                            () -> {
                                new Alert(Alert.AlertType.INFORMATION,
                                        "✅ Cosecha reactivada — en proceso.")
                                        .showAndWait();
                                cargarCosechas();
                            }
                    );
                } else {
                    pedirPrecioYCerrar(cosechaId, nombre);
                }
            });
            return;
        }

        // ✅ Cosecha EN PROCESO → cerrar (con opción de poner en espera)
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "Cerrar definitivamente",
                "Cerrar definitivamente",
                "Poner en espera"
        );
        dialog.setTitle("Cerrar cosecha");
        dialog.setHeaderText("Cosecha: " + nombre);
        dialog.setContentText("¿Qué deseas hacer?");

        dialog.showAndWait().ifPresent(opcion -> {
            if (opcion.equals("Poner en espera")) {
                LoadingUtil.ejecutar(
                        tablaCosechas,
                        "Actualizando estado...",
                        () -> cambiarEstadoCosecha(cosechaId, "en_espera"),
                        () -> {
                            new Alert(Alert.AlertType.INFORMATION,
                                    "Cosecha en espera.")
                                    .showAndWait();
                            cargarCosechas();
                        }
                );
            } else {
                pedirPrecioYCerrar(cosechaId, nombre);
            }
        });
    }

    private void pedirPrecioYCerrar(int cosechaId, String nombre) {
        TextInputDialog dialogPerg = new TextInputDialog("0");
        dialogPerg.setTitle("Peso real de pergamino");
        dialogPerg.setHeaderText(
                "Cosecha: " + nombre + "\n" +
                        "Ingresa el peso REAL del pergamino obtenido:");
        dialogPerg.setContentText("Pergamino real (kg):");

        java.util.Optional<String> respPerg = dialogPerg.showAndWait();
        if (respPerg.isEmpty()) {
            // ✅ Usuario cancela → no se cierra
            return;
        }

        double pergReal;
        try {
            pergReal = Double.parseDouble(respPerg.get().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Ingresa un peso válido.")
                    .showAndWait();
            return;
        }

        try {
            PreparedStatement ps = db.getConexion()
                    .prepareStatement(
                            "UPDATE cosecha_lotes " +
                                    "SET pergamino_real_kg=? " +
                                    "WHERE cosecha_id=?"
                    );
            ps.setDouble(1, pergReal);
            ps.setInt(2, cosechaId);
            ps.executeUpdate();
        } catch (java.sql.SQLException ex) {
            System.out.println("❌ SQL: " + ex.getMessage());
        }

        TextInputDialog dialogPrecio = new TextInputDialog("0");
        dialogPrecio.setTitle("Valor de venta");
        dialogPrecio.setHeaderText(
                "¿Cuál fue el valor pagado por CARGA?\n" +
                        "(1 carga = 125 kg de pergamino)\n" +
                        "Puedes dejar 0 si aún no lo sabes.");
        dialogPrecio.setContentText("Valor por carga $:");

        java.util.Optional<String> respPrecio = dialogPrecio.showAndWait();

        double precioPorKilo = 0;
        if (respPrecio.isPresent()) {
            try {
                double valorCarga = Double.parseDouble(respPrecio.get().trim());
                precioPorKilo = valorCarga / 125.0;
            } catch (NumberFormatException e) {
                precioPorKilo = 0;
            }
        }

        // ✅ SIEMPRE cerrar la cosecha aunque no se haya ingresado precio.
        //    Muestra un reloj mientras se actualiza la BD.
        final double precioFinal = precioPorKilo;
        final double pergFinal   = pergReal;
        LoadingUtil.ejecutar(
                tablaCosechas,
                "Cerrando cosecha...",
                () -> {
                    cambiarEstadoCosecha(cosechaId, "terminado");
                    if (precioFinal > 0) {
                        guardarPrecioVenta(cosechaId, precioFinal);
                    }
                },
                () -> {
                    new Alert(Alert.AlertType.INFORMATION,
                            "✅ Cosecha cerrada.\n" +
                                    "Pergamino real: " + pergFinal + " kg\n" +
                                    (precioFinal > 0
                                            ? "Precio kilo: $" +
                                            String.format("%,.0f", precioFinal)
                                            : "(Sin precio registrado)"))
                            .showAndWait();
                    cargarCosechas();
                }
        );
    }

    private void cambiarEstadoCosecha(int cosechaId, String estado) {
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "UPDATE cosecha SET estado=?, fecha_fin=? WHERE id=?"
            );
            // ✅ Solo guardar fecha_fin si la cosecha se termina.
            //    Al reactivar (en_proceso) o poner en espera, fecha_fin=null.
            String fecha = estado.equals("terminado") ?
                    LocalDate.now().toString() : null;
            ps.setString(1, estado);
            ps.setString(2, fecha);
            ps.setInt(3, cosechaId);
            ps.executeUpdate();

            PreparedStatement ps2 = db.getConexion().prepareStatement(
                    "UPDATE cosecha_lotes SET estado=? WHERE cosecha_id=?"
            );
            ps2.setString(1, estado);
            ps2.setInt(2, cosechaId);
            ps2.executeUpdate();

            System.out.println("✅ Cosecha " + cosechaId +
                    " → estado: " + estado);
        } catch (Exception e) {
            System.out.println("❌ Error estado: " + e.getMessage());
        }
    }

    private void guardarPrecioVenta(int cosechaId, double precio) {
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "UPDATE cosecha SET precio_kilo_venta=?, " +
                            "total_venta=(SELECT SUM(estimado_pergamino_kg) " +
                            "FROM cosecha_lotes WHERE cosecha_id=?) * ? " +
                            "WHERE id=?"
            );
            ps.setDouble(1, precio);
            ps.setInt(2, cosechaId);
            ps.setDouble(3, precio);
            ps.setInt(4, cosechaId);
            ps.executeUpdate();
            System.out.println("✅ Precio venta: $" + precio);
        } catch (Exception e) {
            System.out.println("❌ Error precio: " + e.getMessage());
        }
    }

    @FXML private void cancelar() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
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
package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.SesionUsuario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controlador del Dashboard principal de NUFI.
 * Maneja la navegación entre módulos y las estadísticas.
 */
public class DashboardController {

    @FXML private Label     lblUsuario;
    @FXML private Label     lblBienvenida;
    @FXML private StackPane contenido;

    // Stats
    @FXML private Label statLotes;
    @FXML private Label statTrabajadores;
    @FXML private Label statKilos;
    @FXML private Label statAlertas;

    // Botones nav
    @FXML private ToggleButton btnDashboard;
    @FXML private ToggleButton btnLotes;
    @FXML private ToggleButton btnTrabajadores;
    @FXML private ToggleButton btnJornadas;
    @FXML private ToggleButton btnInventario;
    @FXML private ToggleButton btnCosecha;
    @FXML private ToggleButton btnTiquetes;
    @FXML private ToggleButton btnReportes;
    @FXML private ToggleButton btnIA;

    // =========================================
    // CONEXIÓN ÚNICA
    // =========================================
    private final BaseDatos baseDatos = ConexionDB.getInstance();
    private javafx.collections.ObservableList<javafx.scene.Node> contenidoHome;

    @FXML
    public void initialize() {

        // Mostrar nombre del usuario activo
        if (SesionUsuario.getUsuario() != null) {
            String nombre = SesionUsuario.getUsuario().getNombre();
            lblUsuario.setText("👤 " + nombre);
            lblBienvenida.setText("¡Bienvenido, " + nombre + "!");
        }

        // Agrupar botones nav
        ToggleGroup grupo = new ToggleGroup();
        btnDashboard.setToggleGroup(grupo);
        btnLotes.setToggleGroup(grupo);
        btnTrabajadores.setToggleGroup(grupo);
        btnJornadas.setToggleGroup(grupo);
        btnInventario.setToggleGroup(grupo);
        btnCosecha.setToggleGroup(grupo);
        btnTiquetes.setToggleGroup(grupo);
        btnReportes.setToggleGroup(grupo);
        btnIA.setToggleGroup(grupo);

        btnDashboard.setSelected(true);

        // Cargar estadísticas
        cargarEstadisticas();

        // Guardar contenido home para restaurarlo
        contenidoHome = javafx.collections.FXCollections
                .observableArrayList(contenido.getChildren());
    }

    private void cargarEstadisticas() {
        try {
            var conn = baseDatos.getConexion();
            var st   = conn.createStatement();

            // Contar lotes
            var rs = st.executeQuery("SELECT COUNT(*) FROM lotes");
            if (rs.next()) statLotes.setText(
                    String.valueOf(rs.getInt(1)));

            // Contar trabajadores
            rs = st.executeQuery("SELECT COUNT(*) FROM trabajadores");
            if (rs.next()) statTrabajadores.setText(
                    String.valueOf(rs.getInt(1)));

            // Total kilos cosechados
            rs = st.executeQuery(
                    "SELECT COALESCE(SUM(kilos),0) FROM jornadas " +
                            "WHERE tipo_trabajo = 'recoleccion'"
            );
            if (rs.next()) statKilos.setText(
                    String.format("%.0f", rs.getDouble(1)));

            // Alertas bodega
            rs = st.executeQuery(
                    "SELECT COUNT(*) FROM productos_bodega " +
                            "WHERE stock_actual <= stock_minimo AND activo = 1"
            );
            if (rs.next()) {
                int alertas = rs.getInt(1);
                statAlertas.setText(String.valueOf(alertas));
                if (alertas > 0)
                    statAlertas.setStyle(
                            "-fx-font-size:34px;" +
                                    "-fx-font-weight:bold;" +
                                    "-fx-text-fill:#c1121f;");
            }

        } catch (Exception e) {
            System.out.println("❌ Error cargando stats: "
                    + e.getMessage());
        }
    }

    // =========================================
    // NAVEGACIÓN
    // =========================================
    @FXML private void mostrarDashboard() {
        contenido.getChildren().setAll(contenidoHome);
        cargarEstadisticas();
    }

    @FXML private void mostrarLotes() {
        cargarVista("/fxml/lotes.fxml");
    }

    @FXML private void mostrarTrabajadores() {
        cargarVista("/fxml/trabajadores.fxml");
    }

    @FXML private void mostrarJornadas() {
        cargarVista("/fxml/jornadas.fxml");
    }

    @FXML private void mostrarInventario() {
        cargarVista("/fxml/inventario.fxml");
    }

    @FXML private void mostrarCosecha() {
        cargarVista("/fxml/cosecha.fxml");
    }

    @FXML private void mostrarTiquetes() {
        cargarVista("/fxml/tiquetes.fxml");
    }

    @FXML private void mostrarReportes() {
        cargarVista("/fxml/reportes.fxml");
    }

    @FXML private void mostrarAsistenteIA() {
        cargarVista("/fxml/asistente_ia.fxml");
    }

    // =========================================
    // HELPERS
    // =========================================
    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxml)
            );
            javafx.scene.Node vista = loader.load();
            contenido.getChildren().setAll(vista);
        } catch (Exception e) {
            System.out.println("❌ Error cargando " + fxml
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarProximamente(String modulo) {
        System.out.println("📌 Módulo: " + modulo
                + " — próximamente");
    }

    // =========================================
    // CERRAR SESIÓN
    // =========================================
    @FXML
    private void cerrarSesion() {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Deseas cerrar sesión?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Cerrar sesión");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                ConexionDB.cerrar();
                SesionUsuario.cerrar();
                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/fxml/login.fxml")
                    );
                    Parent root = loader.load();
                    Stage stage = (Stage) lblUsuario
                            .getScene().getWindow();
                    Scene scene = new Scene(root, 900, 600);
                    scene.getStylesheets().add(
                            getClass().getResource(
                                    "/css/nufi-theme.css"
                            ).toExternalForm()
                    );
                    stage.setScene(scene);
                } catch (Exception e) {
                    System.out.println("❌ Error: "
                            + e.getMessage());
                }
            }
        });
    }

    public BaseDatos getBaseDatos() { return baseDatos; }
}
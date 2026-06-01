package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.Lote;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class HistorialLoteController {

    // ── Labels ──
    @FXML private Label lblTituloLote;

    // ── Tabla Jornadas ──
    @FXML private TableView<ObservableList<String>> tablaJornadas;
    @FXML private TableColumn<ObservableList<String>, String> colJFecha;
    @FXML private TableColumn<ObservableList<String>, String> colJTrabajador;
    @FXML private TableColumn<ObservableList<String>, String> colJTipo;
    @FXML private TableColumn<ObservableList<String>, String> colJModo;
    @FXML private TableColumn<ObservableList<String>, String> colJKilos;
    @FXML private TableColumn<ObservableList<String>, String> colJTotal;
    @FXML private TableColumn<ObservableList<String>, String> colJObs;

    // ── Tabla Insumos ──
    @FXML private TableView<ObservableList<String>> tablaInsumos;
    @FXML private TableColumn<ObservableList<String>, String> colIFecha;
    @FXML private TableColumn<ObservableList<String>, String> colIProducto;
    @FXML private TableColumn<ObservableList<String>, String> colICantidad;
    @FXML private TableColumn<ObservableList<String>, String> colITipo;
    @FXML private TableColumn<ObservableList<String>, String> colIObs;

    // ── Tabla Cosechas ──
    @FXML private TableView<ObservableList<String>> tablaCosechas;
    @FXML private TableColumn<ObservableList<String>, String> colCNombre;
    @FXML private TableColumn<ObservableList<String>, String> colCEstado;
    @FXML private TableColumn<ObservableList<String>, String> colCCereza;
    @FXML private TableColumn<ObservableList<String>, String> colCPergamino;
    @FXML private TableColumn<ObservableList<String>, String> colCFecha;

    // ── Resumen ──
    @FXML private Label lblResumenJornadas;
    @FXML private Label lblResumenKilos;
    @FXML private Label lblResumenPagado;
    @FXML private Label lblResumenInsumos;
    @FXML private Label lblResumenCosechas;

    private final BaseDatos db = ConexionDB.getInstance();
    private Lote loteActual;

    public void cargarDatos(Lote lote) {
        this.loteActual = lote;
        lblTituloLote.setText("🌿 Historial completo — Lote: " + lote.nombre);
        cargarJornadas();
        cargarInsumos();
        cargarCosechas();
        cargarResumen();
    }

    private void cargarJornadas() {
        colJFecha.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(0)));
        colJTrabajador.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(1)));
        colJTipo.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(2)));
        colJModo.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(3)));
        colJKilos.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(4)));
        colJTotal.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(5)));
        colJObs.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(6)));

        tablaJornadas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<ObservableList<String>> datos =
                FXCollections.observableArrayList();
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT j.fecha, t.nombre, j.tipo_trabajo, " +
                            "j.modo_pago, j.kilos, j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN trabajadores t ON j.trabajador_id = t.id " +
                            "WHERE j.lote_id = ? ORDER BY j.fecha DESC"
            );
            ps.setInt(1, loteActual.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> fila =
                        FXCollections.observableArrayList();
                fila.add(rs.getString("fecha"));
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("tipo_trabajo"));
                fila.add(rs.getString("modo_pago"));
                fila.add(String.format("%.1f", rs.getDouble("kilos")));
                fila.add("$" + String.format("%,.0f",
                        rs.getDouble("total_pagar")));
                fila.add(rs.getString("observaciones"));
                datos.add(fila);
            }
        } catch (Exception e) {
            System.out.println("❌ Error jornadas lote: " + e.getMessage());
        }
        tablaJornadas.setItems(datos);
    }

    private void cargarInsumos() {
        colIFecha.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(0)));
        colIProducto.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(1)));
        colICantidad.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(2)));
        colITipo.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(3)));
        colIObs.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(4)));

        tablaInsumos.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<ObservableList<String>> datos =
                FXCollections.observableArrayList();
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT mb.fecha, pb.nombre, mb.cantidad, " +
                            "mb.tipo_movimiento, mb.observaciones " +
                            "FROM movimientos_bodega mb " +
                            "JOIN productos_bodega pb ON mb.producto_id = pb.id " +
                            "WHERE mb.lote_id = ? ORDER BY mb.fecha DESC"
            );
            ps.setInt(1, loteActual.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> fila =
                        FXCollections.observableArrayList();
                fila.add(rs.getString("fecha"));
                fila.add(rs.getString("nombre"));
                fila.add(String.format("%.1f", rs.getDouble("cantidad")));
                fila.add(rs.getString("tipo_movimiento"));
                fila.add(rs.getString("observaciones"));
                datos.add(fila);
            }
        } catch (Exception e) {
            System.out.println("❌ Error insumos lote: " + e.getMessage());
        }
        tablaInsumos.setItems(datos);
    }

    private void cargarCosechas() {
        colCNombre.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(0)));
        colCEstado.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(1)));
        colCCereza.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(2)));
        colCPergamino.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(3)));
        colCFecha.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().get(4)));

        tablaCosechas.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<ObservableList<String>> datos =
                FXCollections.observableArrayList();
        try {
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT c.nombre, cl.estado, cl.total_cereza_kg, " +
                            "cl.estimado_pergamino_kg, cl.fecha_inicio " +
                            "FROM cosecha_lotes cl " +
                            "JOIN cosecha c ON cl.cosecha_id = c.id " +
                            "WHERE cl.lote_id = ? ORDER BY cl.fecha_inicio DESC"
            );
            ps.setInt(1, loteActual.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ObservableList<String> fila =
                        FXCollections.observableArrayList();
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("estado"));
                fila.add(String.format("%.1f", rs.getDouble("total_cereza_kg")) + " kg");
                fila.add(String.format("%.1f", rs.getDouble("estimado_pergamino_kg")) + " kg");
                fila.add(rs.getString("fecha_inicio") != null ?
                        rs.getString("fecha_inicio") : "—");
                datos.add(fila);
            }
        } catch (Exception e) {
            System.out.println("❌ Error cosechas lote: " + e.getMessage());
        }
        tablaCosechas.setItems(datos);
    }

    private void cargarResumen() {
        try {
            // Jornadas
            PreparedStatement ps = db.getConexion().prepareStatement(
                    "SELECT COUNT(*) as total, " +
                            "COALESCE(SUM(kilos),0) as kilos, " +
                            "COALESCE(SUM(total_pagar),0) as pagado " +
                            "FROM jornadas WHERE lote_id=?"
            );
            ps.setInt(1, loteActual.id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblResumenJornadas.setText("👷 Total jornadas: " +
                        rs.getInt("total"));
                lblResumenKilos.setText("☕ Total kilos recolectados: " +
                        String.format("%.1f", rs.getDouble("kilos")) + " kg");
                lblResumenPagado.setText("💰 Total pagado trabajadores: $" +
                        String.format("%,.0f", rs.getDouble("pagado")));
            }

            // Insumos
            PreparedStatement ps2 = db.getConexion().prepareStatement(
                    "SELECT COUNT(*) as total FROM movimientos_bodega WHERE lote_id=?"
            );
            ps2.setInt(1, loteActual.id);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                lblResumenInsumos.setText("📦 Total movimientos de insumos: " +
                        rs2.getInt("total"));
            }

            // Cosechas
            PreparedStatement ps3 = db.getConexion().prepareStatement(
                    "SELECT COUNT(*) as total FROM cosecha_lotes WHERE lote_id=?"
            );
            ps3.setInt(1, loteActual.id);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                lblResumenCosechas.setText("🌱 Total cosechas: " +
                        rs3.getInt("total"));
            }

        } catch (Exception e) {
            System.out.println("❌ Error resumen: " + e.getMessage());
        }
    }
}
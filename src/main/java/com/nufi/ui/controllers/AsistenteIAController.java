package com.nufi.ui.controllers;

import com.nufi.AsistenteIA;
import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.SesionUsuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

public class AsistenteIAController {

    @FXML private ScrollPane scrollChat;
    @FXML private VBox       contenedorMensajes;
    @FXML private HBox       panelEscribiendo;
    @FXML private TextField  txtPregunta;

    private final BaseDatos  db = ConexionDB.getInstance();
    private final AsistenteIA ia = new AsistenteIA();

    @FXML
    public void initialize() {
        // Mensaje de bienvenida
        agregarMensajeIA(
                "¡Hola! Soy NUFI IA ☕\n\n" +
                        "Puedo ayudarte con información sobre:\n" +
                        "• Estado de la cosecha actual\n" +
                        "• Producción de pergamino estimada\n" +
                        "• Stock del inventario\n" +
                        "• Pagos pendientes a trabajadores\n\n" +
                        "¿En qué te puedo ayudar hoy?"
        );

        // Cargar historial previo
        cargarHistorial();
    }

    private void cargarHistorial() {
        if (SesionUsuario.getUsuario() == null) return;
        try {
            java.sql.PreparedStatement ps = db.getConexion()
                    .prepareStatement(
                            "SELECT pregunta, respuesta FROM chat_historial " +
                                    "WHERE usuario_id = ? " +
                                    "ORDER BY fecha ASC, hora ASC LIMIT 10"
                    );
            ps.setInt(1, SesionUsuario.getUsuario().id);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                agregarMensajeUsuario(rs.getString("pregunta"));
                agregarMensajeIA(rs.getString("respuesta"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error historial: " + e.getMessage());
        }
    }

    @FXML
    private void enviarPregunta() {
        String pregunta = txtPregunta.getText().trim();
        if (pregunta.isEmpty()) return;

        txtPregunta.clear();
        agregarMensajeUsuario(pregunta);
        mostrarEscribiendo(true);

        // Llamar IA en hilo separado
        new Thread(() -> {
            int usuarioId = SesionUsuario.getUsuario() != null ?
                    SesionUsuario.getUsuario().id : 1;

            String respuesta = ia.preguntarConDatos(
                    pregunta, db, usuarioId
            );

            Platform.runLater(() -> {
                mostrarEscribiendo(false);
                agregarMensajeIA(respuesta);
                scrollAlFinal();
            });
        }, "nufi-ia-thread").start();
    }

    private void agregarMensajeUsuario(String texto) {
        // Burbuja usuario — derecha
        Label lblMensaje = new Label(texto);
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(420);
        lblMensaje.setStyle(
                "-fx-background-color:#2d6a4f;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:12 12 2 12;" +
                        "-fx-padding:10 14;" +
                        "-fx-font-size:13px;"
        );

        HBox contenedor = new HBox(lblMensaje);
        contenedor.setAlignment(Pos.CENTER_RIGHT);

        // Nombre usuario
        Label lblNombre = new Label("Tú");
        lblNombre.setStyle(
                "-fx-font-size:10px;" +
                        "-fx-text-fill:#999;" +
                        "-fx-padding:2 4 0 0;"
        );
        HBox header = new HBox(lblNombre);
        header.setAlignment(Pos.CENTER_RIGHT);

        VBox bloque = new VBox(2, header, contenedor);
        contenedorMensajes.getChildren().add(bloque);
        scrollAlFinal();
    }

    private void agregarMensajeIA(String texto) {
        // Burbuja IA — izquierda
        Label lblMensaje = new Label(texto);
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(420);
        lblMensaje.setTextAlignment(TextAlignment.LEFT);
        lblMensaje.setStyle(
                "-fx-background-color:#f0f4f0;" +
                        "-fx-text-fill:#1a1a1a;" +
                        "-fx-background-radius:12 12 12 2;" +
                        "-fx-padding:10 14;" +
                        "-fx-font-size:13px;" +
                        "-fx-border-color:#d4e8d4;" +
                        "-fx-border-radius:12 12 12 2;" +
                        "-fx-border-width:1;"
        );

        HBox contenedor = new HBox(lblMensaje);
        contenedor.setAlignment(Pos.CENTER_LEFT);

        // Nombre IA
        Label lblNombre = new Label("🤖 NUFI IA");
        lblNombre.setStyle(
                "-fx-font-size:10px;" +
                        "-fx-text-fill:#2d6a4f;" +
                        "-fx-font-weight:bold;" +
                        "-fx-padding:2 0 0 4;"
        );
        HBox header = new HBox(lblNombre);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox bloque = new VBox(2, header, contenedor);
        contenedorMensajes.getChildren().add(bloque);
        scrollAlFinal();
    }

    private void mostrarEscribiendo(boolean mostrar) {
        panelEscribiendo.setVisible(mostrar);
        panelEscribiendo.setManaged(mostrar);
    }

    private void scrollAlFinal() {
        Platform.runLater(() ->
                scrollChat.setVvalue(1.0));
    }

    @FXML
    private void limpiarChat() {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Limpiar el historial del chat?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Limpiar chat");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                contenedorMensajes.getChildren().clear();
                agregarMensajeIA(
                        "Chat limpiado. ¡Hola de nuevo! ☕\n" +
                                "¿En qué te puedo ayudar?"
                );
            }
        });
    }

    @FXML
    private void preguntaRapida1() {
        txtPregunta.setText("¿Cómo va la cosecha actual?");
        enviarPregunta();
    }

    @FXML
    private void preguntaRapida2() {
        txtPregunta.setText(
                "¿Hay productos con stock bajo en el inventario?");
        enviarPregunta();
    }

    @FXML
    private void preguntaRapida3() {
        txtPregunta.setText(
                "¿Cuánto dinero debo pagar a los trabajadores?");
        enviarPregunta();
    }
}
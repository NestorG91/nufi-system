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

public class AsistenteIAController {

    @FXML private ScrollPane scrollChat;
    @FXML private VBox       contenedorMensajes;
    @FXML private HBox       panelEscribiendo;
    @FXML private TextField  txtPregunta;

    private final BaseDatos   db = ConexionDB.getInstance();
    private final AsistenteIA ia = new AsistenteIA();
    private String climaActual = "";

    @FXML
    public void initialize() {
        // ✅ Bienvenida inmediata sin consultar el clima (ahorra créditos
        //    y evita la espera). El clima se pide solo con el botón rápido.
        agregarMensajeIA(
                "¡Hola! Soy NUFI IA ☕\n" +
                        "¿En qué te puedo ayudar hoy?\n\n" +
                        "Usa el botón \"☀ ¿Clima hoy?\" si necesitas el pronóstico."
        );
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

        String lower = pregunta.toLowerCase();
        boolean esClima = (lower.contains("clima") &&
                (lower.contains("hoy") ||
                        lower.contains("actual") ||
                        lower.contains("ahora"))) ||
                lower.equals("clima") ||
                lower.contains("temperatura actual") ||
                lower.contains("como esta el tiempo") ||
                lower.contains("cómo está el tiempo") ||
                lower.contains("va a llover") ||
                lower.contains("va llover");

        if (esClima) {
            new Thread(() -> {
                String clima = ia.obtenerClimaActual();
                Platform.runLater(() -> {
                    mostrarEscribiendo(false);
                    agregarMensajeIA(clima);
                    scrollAlFinal();
                });
            }, "nufi-clima-thread").start();
            return;
        }

        // ✅ Claude API — el clima solo se adjunta si el usuario lo cargó
        //    explícitamente con el botón rápido (ahorra tokens en cada pregunta).
        new Thread(() -> {
            int usuarioId = SesionUsuario.getUsuario() != null ?
                    SesionUsuario.getUsuario().id : 1;

            String preguntaFinal = pregunta;
            if (!climaActual.isEmpty()) {
                String climaResumen = climaActual
                        .lines()
                        .filter(l -> l.contains("Temperatura") ||
                                l.contains("Lluvia") ||
                                l.contains("Condición") ||
                                l.contains("Humedad") ||
                                l.contains("PRÓXIMOS"))
                        .limit(6)
                        .reduce("", (a, b) -> a + b + "\n");

                preguntaFinal =
                        "CLIMA HOY EN LA FINCA (Albania, Santander):\n" +
                                climaResumen + "\n" +
                                "PREGUNTA:\n" + pregunta;
            }

            String respuesta = ia.preguntarConDatos(
                    preguntaFinal, db, usuarioId);
            Platform.runLater(() -> {
                mostrarEscribiendo(false);
                agregarMensajeIA(respuesta);
                scrollAlFinal();
            });
        }, "nufi-ia-thread").start();
    }

    private void agregarMensajeUsuario(String texto) {
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
        TextArea txtMensaje = new TextArea(texto);
        txtMensaje.setWrapText(true);
        txtMensaje.setEditable(false);
        txtMensaje.setMaxWidth(500);
        txtMensaje.setPrefWidth(500);
        txtMensaje.setPrefRowCount(
                Math.min(texto.split("\n").length + 1, 20));
        txtMensaje.setStyle(
                "-fx-background-color:#f0f4f0;" +
                        "-fx-border-color:#d4e8d4;" +
                        "-fx-border-radius:12;" +
                        "-fx-background-radius:12;" +
                        "-fx-font-size:13px;" +
                        "-fx-control-inner-background:#f0f4f0;"
        );
        HBox contenedor = new HBox(txtMensaje);
        contenedor.setAlignment(Pos.CENTER_LEFT);

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
        Platform.runLater(() -> scrollChat.setVvalue(1.0));
    }

    @FXML
    private void limpiarChat() {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Limpiar el historial del chat? Esta accion borra " +
                        "tambien las conversaciones guardadas en la base de datos.",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Limpiar chat");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // 1) Limpiar UI
                contenedorMensajes.getChildren().clear();

                // 2) Borrar historial persistente en BD para que al volver a
                //    entrar al modulo no se recargue la conversacion anterior.
                if (SesionUsuario.getUsuario() != null) {
                    db.limpiarHistorialChat(SesionUsuario.getUsuario().id);
                }

                // 3) Mensaje de bienvenida nuevo
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

    @FXML
    private void preguntaRapida4() {
        mostrarEscribiendo(true);
        new Thread(() -> {
            String clima = ia.obtenerClimaActual();
            climaActual = clima; // ✅ se cachea para preguntas posteriores
            Platform.runLater(() -> {
                mostrarEscribiendo(false);
                agregarMensajeIA(clima);
                scrollAlFinal();
            });
        }, "nufi-clima-thread").start();
    }
}
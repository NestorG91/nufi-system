package com.nufi.ui.controllers;

import com.nufi.BaseDatos;
import com.nufi.ConexionDB;
import com.nufi.SesionUsuario;
import com.nufi.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;
    @FXML private Label         lblError;

    // =========================================
    // CONEXIÓN ÚNICA — una sola declaración
    // =========================================
    private final BaseDatos db = ConexionDB.getInstance();

    @FXML
    private void handleLogin() {
        String usuario  = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Ingresa usuario y contraseña.");
            return;
        }

        btnLogin.setDisable(true);
        btnLogin.setText("Verificando...");
        lblError.setVisible(false);

        new Thread(() -> {

            // ✅ usa db, no baseDatos
            Usuario usuarioAutenticado = db.login(usuario, password);

            Platform.runLater(() -> {
                if (usuarioAutenticado != null) {
                    irAlDashboard(usuarioAutenticado);
                } else {
                    mostrarError("Usuario o contraseña incorrectos.");
                    btnLogin.setDisable(false);
                    btnLogin.setText("Ingresar");
                }
            });

        }, "nufi-login-thread").start();
    }

    private void irAlDashboard(Usuario usuarioAutenticado) {
        try {
            SesionUsuario.iniciar(usuarioAutenticado);

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/css/nufi-theme.css")
                            .toExternalForm()
            );
            stage.setResizable(true);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            mostrarError("Error al cargar dashboard: " + e.getMessage());
            btnLogin.setDisable(false);
            btnLogin.setText("Ingresar");
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
    }
}
package com.nufi.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void iniciarSesion() {

        String usuario = txtUsuario.getText();

        String password = txtPassword.getText();

        System.out.println("Usuario: " + usuario);

        System.out.println("Password: " + password);
    }
}
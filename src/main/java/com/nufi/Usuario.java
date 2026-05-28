package com.nufi;

import org.mindrot.jbcrypt.BCrypt;

public class Usuario {

    int id;
    String nombre;
    String usuario;
    String contrasena;
    String rol;
    int activo;

    // Constructor
    public Usuario(String nombre, String usuario, String contrasena, String rol) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = encriptarContrasena(contrasena);
        this.rol = rol;
        this.activo = 1;
    }

    // Encriptar contraseña
    public static String encriptarContrasena(String contrasena) {
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }

    // Verificar contraseña
    public static boolean verificarContrasena(String contrasenaIngresada, String hashGuardado) {
        return BCrypt.checkpw(contrasenaIngresada, hashGuardado);
    }

    // Mostrar info
    public void mostrarInf() {
        System.out.println(
                "Usuario: " + usuario +
                        " | Nombre: " + nombre +
                        " | Rol: " + rol
        );
    }

    // GETTERS
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }

    public int getActivo() {
        return activo;
    }
}
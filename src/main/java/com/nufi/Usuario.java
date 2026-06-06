package com.nufi;

import org.mindrot.jbcrypt.BCrypt;

public class Usuario {

    // =========================================
    // ATRIBUTOS
    // =========================================
    public int    id;
    public String nombre;
    public String usuario;
    public String contrasena;
    public String rol;
    public int    activo;

    // =========================================
    // CONSTRUCTOR
    // =========================================
    public Usuario(String nombre, String usuario,
                   String contrasena, String rol) {
        this.nombre     = nombre;
        this.usuario    = usuario;
        this.contrasena = encriptarContrasena(contrasena);
        this.rol        = rol;
        this.activo     = 1;
    }

    // =========================================
    // MÉTODOS ESTÁTICOS
    // =========================================
    public static String encriptarContrasena(String contrasena) {
        return BCrypt.hashpw(contrasena, BCrypt.gensalt());
    }

    public static boolean verificarContrasena(
            String contrasenaIngresada, String hashGuardado) {
        return BCrypt.checkpw(contrasenaIngresada, hashGuardado);
    }

    // =========================================
    // GETTERS
    // =========================================
    public int    getId()      { return id; }
    public String getNombre()  { return nombre; }
    public String getUsuario() { return usuario; }
    public String getRol()     { return rol; }
    public int    getActivo()  { return activo; }

    public void mostrarInf() {
        System.out.println(
                "Usuario: "  + usuario +
                        " | Nombre: " + nombre +
                        " | Rol: "    + rol
        );
    }
}
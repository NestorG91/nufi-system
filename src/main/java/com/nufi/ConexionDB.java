package com.nufi;

public class ConexionDB {

    private static BaseDatos instancia = null;

    // Una sola instancia para toda la app
    public static BaseDatos getInstance() {
        if (instancia == null) {
            instancia = new BaseDatos();
            instancia.conectar();
        }
        return instancia;
    }

    public static void cerrar() {
        if (instancia != null) {
            instancia.cerrar();
            instancia = null;
        }
    }
}
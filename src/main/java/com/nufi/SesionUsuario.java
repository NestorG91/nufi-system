package com.nufi;

/**
 * Clase estática que guarda el usuario autenticado
 * durante toda la sesión de la app.
 */
public class SesionUsuario {

    private static Usuario usuarioActivo;

    public static void iniciar(Usuario u)  { usuarioActivo = u; }
    public static Usuario getUsuario()     { return usuarioActivo; }
    public static void cerrar()            { usuarioActivo = null; }
    public static boolean estaActivo()     { return usuarioActivo != null; }
}
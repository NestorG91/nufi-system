package com.nufi;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {

        // ========================================
        // BIENVENIDA NUFI
        // ========================================
        System.out.println("☕ Bienvenido al sistema NUFI");
        System.out.println("================================");
        System.out.println("Finca: La Quinta");
        System.out.println("Propietarios: Filimon y Nubia");
        System.out.println("Ubicación: Vereda Albania, Santander, Colombia");
        System.out.println("Hectáreas: 1.2");
        System.out.println("Producto: Café");
        System.out.println();

        // ========================================
        // CONEXIÓN BASE DE DATOS
        // ========================================
        BaseDatos db = new BaseDatos();
        db.conectar();

        // ========================================
        // CREAR TODAS LAS TABLAS
        // ========================================
        db.crearTabla();
        db.crearTablasTrabajadores();
        db.crearTablasInventario();
        db.crearTablaTiquetes();
        db.crearTablasCosecha();
        db.crearTablaUsuarios();
        db.crearTablaChatHistorial();

        // ========================================
        // LOTES
        // ========================================
        Lote[] lotes = {
                new Lote("Blanca",     850,   "Mayo-Junio 2020"),
                new Lote("Cachipai",   2500,  "Mayo-Junio 2020"),
                new Lote("Rebeca",     1250,  "Mayo-Junio 2020"),
                new Lote("Yary",       12000, "Mayo-Junio 2020"),
                new Lote("El Avion 1", 1200,  "Mayo-Junio 2020"),
                new Lote("El Avion 2", 3000,  "Diciembre 2025")
        };

        for (Lote lote : lotes) {
            db.guardarLote(lote);
        }
        db.listarLotes();

        // ========================================
        // TRABAJADORES
        // ========================================
        Trabajador[] trabajadores = {
                new Trabajador("Carlos Rueda",  "12345678", "3101234567", "Vereda Albania"),
                new Trabajador("Pedro Gomez",   "23456789", "3209876543", "Vereda El Cairo"),
                new Trabajador("Luis Martinez", "34567890", "3156789012", "Vereda La Palma")
        };

        for (Trabajador t : trabajadores) {
            db.guardarTrabajador(t);
        }
        db.listarTrabajadores();

        // ========================================
        // JORNADAS
        // ========================================
        Jornada[] jornadas = {
                new Jornada(1, 4, "2026-05-12", "recoleccion", "kilo",
                        85, 0, 1200, "Buen trabajo"),
                new Jornada(2, 1, "2026-05-12", "recoleccion", "dia",
                        0, 45000, 0, "Trabajo completo"),
                new Jornada(3, 2, "2026-05-12", "abono", "dia",
                        0, 45000, 0, "Abono triple 15")
        };

        for (Jornada j : jornadas) {
            db.guardarJornada(j);
        }
        db.listarJornadas();

        // ========================================
        // INVENTARIO BODEGA
        // ========================================
        Producto[] productos = {
                new Producto("Abono Triple 15", "abono",       "bultos", 10.0, 3.0, 85000.0),
                new Producto("Glifosato",       "veneno",      "litros", 5.0,  2.0, 45000.0),
                new Producto("Lorban",          "veneno",      "litros", 8.0,  2.0, 52000.0),
                new Producto("Machete",         "herramienta", "unidad", 4.0,  1.0, 35000.0)
        };

        for (Producto p : productos) {
            db.guardarProducto(p);
        }
        db.listarProductos();

        // ========================================
        // COSECHA — descomentar solo la primera vez
        // ========================================
        Cosecha cosecha = new Cosecha("Cosecha Mayor 2026", "2026-05-12", "Primera cosecha del año");
        int cosechaId = db.guardarCosecha(cosecha);

        CosechaLote loteYary   = new CosechaLote(cosechaId, 4, "2026-05-12");
        CosechaLote loteBlanca = new CosechaLote(cosechaId, 1, "2026-05-12");

        loteYary.observaciones   = "Lote principal";
        loteBlanca.observaciones = "Lote secundario";

        db.guardarCosechaLote(loteYary);
        db.guardarCosechaLote(loteBlanca);

        db.actualizarCerezaLote(1, 85.0);
        db.actualizarCerezaLote(2, 45.0);

        // Ver resumen cosecha
        db.verResumenCosecha(1);

        // ========================================
        // REPORTES
        // ========================================
        db.historialPorLote(4);
        db.historialPorTrabajador(1);

        // ========================================
        // USUARIOS
        // ========================================
        db.guardarUsuario(new Usuario("Nubia",          "nubia",   "nufi2026", "administrador"));
        db.guardarUsuario(new Usuario("Filimon Duarte", "filimon", "nufi2026", "operario"));

        // ========================================
        // LOGIN
        // ========================================
        System.out.println("\n🔐 Probando Login:");
        System.out.println("================================");
        db.login("nubia", "nufi2026");
        db.login("nubia", "clavemala");

        // ========================================
        // ASISTENTE IA
        // ========================================
        AsistenteIA ia = new AsistenteIA();
        int usuarioId = 1; // Nubia

//        System.out.println("\n🤖 ASISTENTE IA NUFI:");
//        System.out.println("================================");
//
//        System.out.println("\n❓ ¿Cuál fue la ganancia neta estimada?\n");
//        System.out.println(ia.preguntarConDatos(
//                "¿Cuál fue la ganancia neta estimada?",
//                db,
//                usuarioId
//        ));

        // ========================================
        // HISTORIAL CHAT
        // ========================================
        db.listarChatHistorial(usuarioId);

        // ========================================
        // CERRAR BD
        // ========================================
        db.cerrar();
    }
}
package com.nufi;

import java.sql.*;

public class BaseDatos {

    private Connection conexion;

    // =========================================
    // CONECTAR A LA BASE DE DATOS
    // =========================================
    public void conectar() {
        try {
            conexion = DriverManager.getConnection("jdbc:sqlite:nufi.db");
            Statement st = conexion.createStatement();
            st.execute("PRAGMA foreign_keys = ON");
            System.out.println("✅ Base de datos conectada");
        } catch (Exception e) {
            System.out.println("❌ Error al conectar: " + e.getMessage());
        }
    }

    // =========================================
    // CREAR TABLAS
    // =========================================
    public void crearTabla() {
        try {
            Statement st = conexion.createStatement();
            st.execute("""
                CREATE TABLE IF NOT EXISTS lotes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    matas INTEGER,
                    fechaSiembra TEXT)
            """);
            System.out.println("✅ Tabla lotes lista");
        } catch (Exception e) {
            System.out.println("❌ Error al crear tabla: " + e.getMessage());
        }
    }

    public void crearTablasTrabajadores() {
        try {
            Statement st = conexion.createStatement();
            // TABLA TRABAJADORES
            st.execute("""
                CREATE TABLE IF NOT EXISTS trabajadores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    cedula TEXT UNIQUE,
                    telefono TEXT,
                    direccion TEXT)
            """);
            // TABLA JORNADAS
            st.execute("""
                CREATE TABLE IF NOT EXISTS jornadas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trabajador_id INTEGER,
                    lote_id INTEGER,
                    fecha TEXT,
                    tipo_trabajo TEXT,
                    modo_pago TEXT,
                    kilos REAL,
                    valor_dia REAL,
                    valor_kilo REAL,
                    total_pagar REAL,
                    observaciones TEXT,
                    FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id),
                    FOREIGN KEY (lote_id) REFERENCES lotes(id))
            """);
            System.out.println("✅ Tablas trabajadores y jornadas listas");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void crearTablasInventario() {
        try {
            Statement st = conexion.createStatement();
            // TABLA PRODUCTOS_BODEGA
            st.execute("""
                CREATE TABLE IF NOT EXISTS productos_bodega (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    tipo TEXT,
                    unidad_medida TEXT,
                    stock_actual REAL,
                    stock_minimo REAL,
                    precio_unidad REAL,
                    activo INTEGER DEFAULT 1)
            """);
            // TABLA MOVIMIENTOS_BODEGA
            st.execute("""
                CREATE TABLE IF NOT EXISTS movimientos_bodega (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    producto_id INTEGER,
                    lote_id INTEGER,
                    tipo_movimiento TEXT,
                    cantidad REAL,
                    fecha TEXT,
                    observaciones TEXT,
                    FOREIGN KEY (producto_id) REFERENCES productos_bodega(id),
                    FOREIGN KEY (lote_id) REFERENCES lotes(id))
            """);
            System.out.println("✅ Tablas inventario bodega listas");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void crearTablaTiquetes() {
        try {
            Statement st = conexion.createStatement();
            st.execute("""
                CREATE TABLE IF NOT EXISTS tiquetes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero_tiquete INTEGER,
                    jornada_id INTEGER,
                    trabajador_id INTEGER,
                    fecha_pago TEXT,
                    total_pagado TEXT,
                    impreso INTEGER DEFAULT 0,
                    FOREIGN KEY (jornada_id) REFERENCES jornadas(id),
                    FOREIGN KEY (trabajador_id) REFERENCES trabajadores(id))
            """);
            System.out.println("✅ Tabla tiquetes lista");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void crearTablasCosecha() {
        try {
            Statement st = conexion.createStatement();
            // TABLA COSECHA
            st.execute("""
                CREATE TABLE IF NOT EXISTS cosecha (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    fecha_inicio TEXT,
                    fecha_fin TEXT,
                    estado TEXT,
                    total_cereza_kg REAL,
                    estimado_pergamino_kg REAL,
                    pergamino_real_kg REAL,
                    precio_kilo_venta REAL,
                    total_venta REAL,
                    gastos_trabajadores REAL,
                    ganancia_neta REAL,
                    observaciones TEXT)
            """);
            // TABLA COSECHA_LOTES
            st.execute("""
                CREATE TABLE IF NOT EXISTS cosecha_lotes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cosecha_id INTEGER,
                    lote_id INTEGER,
                    fecha_inicio TEXT,
                    fecha_fin TEXT,
                    estado TEXT,
                    total_cereza_kg REAL,
                    estimado_pergamino_kg REAL,
                    pergamino_real_kg REAL,
                    observaciones TEXT,
                    FOREIGN KEY (cosecha_id) REFERENCES cosecha(id),
                    FOREIGN KEY (lote_id) REFERENCES lotes(id))
            """);
            System.out.println("✅ Tablas cosecha listas");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void crearTablaUsuarios() {
        try {
            Statement st = conexion.createStatement();
            st.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    usuario TEXT UNIQUE,
                    contrasena TEXT,
                    rol TEXT,
                    activo INTEGER DEFAULT 1)
            """);
            System.out.println("✅ Tabla usuarios lista");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void crearTablaChatHistorial() {
        try {
            Statement st = conexion.createStatement();
            st.execute("""
                CREATE TABLE IF NOT EXISTS chat_historial (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    usuario_id INTEGER,
                    pregunta TEXT,
                    respuesta TEXT,
                    fecha TEXT,
                    hora TEXT,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id))
            """);
            System.out.println("✅ Tabla chat historial lista");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // Crear usuarios iniciales si no existen
    public void crearUsuariosIniciales() {
        try {
            PreparedStatement check = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM usuarios"
            );
            ResultSet rs = check.executeQuery();
            if (rs.getInt(1) == 0) {
                guardarUsuario(new Usuario(
                        "Nubia Pabuence", "nubia", "nufi2026", "administrador"
                ));
                guardarUsuario(new Usuario(
                        "Filimon Gelvez", "filimon", "nufi2026", "operario"
                ));
                System.out.println("✅ Usuarios iniciales creados");
            }
        } catch (Exception e) {
            System.out.println("❌ Error usuarios iniciales: " + e.getMessage());
        }
    }

    // =========================================
    // INICIALIZAR BD COMPLETA
    // =========================================
    public void inicializarBD() {
        crearTabla();
        crearTablasTrabajadores();
        crearTablasInventario();
        crearTablaTiquetes();
        crearTablasCosecha();
        crearTablaUsuarios();
        crearTablaChatHistorial();
        crearUsuariosIniciales();
        crearTrabajadorDuenos();
        System.out.println("✅ Base de datos NUFI inicializada correctamente");
    }

    // =========================================
    // LOTES
    // =========================================
    public boolean loteExiste(String nombre) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM lotes WHERE nombre = ?"
            );
            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("❌ Error al verificar lote: " + e.getMessage());
            return false;
        }
    }
    // ✅ Trabajador especial dueños de finca
    private void crearTrabajadorDuenos() {
        try {
            ResultSet rs = conexion.createStatement().executeQuery(
                    "SELECT id FROM trabajadores WHERE nombre='Dueño Finca'"
            );
            if (!rs.next()) {
                conexion.createStatement().executeUpdate(
                        "INSERT INTO trabajadores " +
                                "(nombre, cedula, telefono) " +
                                "VALUES ('Dueño Finca', '000000', '000000')"
                );
                System.out.println("✅ Trabajador Dueño Finca creado");
            }
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
    public void guardarLote(Lote lote) {
        try {
            if (loteExiste(lote.nombre)) {
                PreparedStatement ps = conexion.prepareStatement(
                        "UPDATE lotes SET matas=?, fechaSiembra=? WHERE nombre=?"
                );
                ps.setInt(1, lote.matas);
                ps.setString(2, lote.fechaSiembra);
                ps.setString(3, lote.nombre);
                ps.executeUpdate();
                System.out.println("🔄 Lote '" + lote.nombre + "' actualizado");
            } else {
                PreparedStatement ps = conexion.prepareStatement(
                        "INSERT INTO lotes (nombre, matas, fechaSiembra) VALUES (?, ?, ?)"
                );
                ps.setString(1, lote.nombre);
                ps.setInt(2, lote.matas);
                ps.setString(3, lote.fechaSiembra);
                ps.executeUpdate();
                System.out.println("✅ Lote '" + lote.nombre + "' guardado");
            }
        } catch (Exception e) {
            System.out.println("❌ Error al guardar lote: " + e.getMessage());
        }
    }

    public void actualizarLote(int id, String nombre, int matas, String fechaSiembra) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE lotes SET nombre=?, matas=?, fechaSiembra=? WHERE id=?"
            );
            ps.setString(1, nombre);
            ps.setInt(2, matas);
            ps.setString(3, fechaSiembra);
            ps.setInt(4, id);
            ps.executeUpdate();
            System.out.println("✅ Lote actualizado");
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar lote: " + e.getMessage());
        }
    }

    public java.util.List<Lote> obtenerLotesConKilos() {
        java.util.List<Lote> lista = new java.util.ArrayList<>();
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT l.id, l.nombre, l.matas, l.fechaSiembra, " +
                            "COALESCE(SUM(j.kilos), 0) as kilos_cosechados " +
                            "FROM lotes l " +
                            "LEFT JOIN jornadas j ON l.id = j.lote_id " +
                            "AND j.tipo_trabajo = 'recoleccion' " +
                            "GROUP BY l.id, l.nombre, l.matas, l.fechaSiembra"
            );
            while (rs.next()) {
                Lote lote = new Lote(
                        rs.getString("nombre"),
                        rs.getInt("matas"),
                        rs.getString("fechaSiembra")
                );
                lote.id = rs.getInt("id");
                lote.kilosCosechados = rs.getDouble("kilos_cosechados");
                lista.add(lote);
            }
        } catch (Exception e) {
            System.out.println("❌ Error obteniendo lotes: " + e.getMessage());
        }
        return lista;
    }

    public void listarLotes() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM lotes");
            System.out.println("\n📋 Lotes guardados en base de datos:");
            System.out.println("================================");
            while (rs.next()) {
                System.out.println(" Lote: " + rs.getString("nombre") +
                        " | Matas: " + rs.getString("matas") +
                        " | Sembrado: " + rs.getString("fechaSiembra"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error al listar lotes: " + e.getMessage());
        }
    }

    // =========================================
    // TRABAJADORES
    // =========================================
    public void guardarTrabajador(Trabajador t) {
        try {
            PreparedStatement check = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM trabajadores WHERE cedula = ?"
            );
            check.setString(1, t.cedula);
            ResultSet rs = check.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("⚠️ Trabajador " + t.nombre + " ya existe, omitiendo...");
                return;
            }
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO trabajadores (nombre, cedula, telefono, direccion) VALUES (?, ?, ?, ?)"
            );
            ps.setString(1, t.nombre);
            ps.setString(2, t.cedula);
            ps.setString(3, t.telefono);
            ps.setString(4, t.direccion);
            ps.executeUpdate();
            System.out.println("✅ Trabajador " + t.nombre + " guardado");
        } catch (Exception e) {
            System.out.println("❌ Error al guardar trabajador: " + e.getMessage());
        }
    }

    public void actualizarTrabajador(int id, String nombre,
                                     String cedula, String telefono, String direccion) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE trabajadores SET nombre=?, cedula=?, telefono=?, direccion=? WHERE id=?"
            );
            ps.setString(1, nombre);
            ps.setString(2, cedula);
            ps.setString(3, telefono);
            ps.setString(4, direccion);
            ps.setInt(5, id);
            ps.executeUpdate();
            System.out.println("✅ Trabajador actualizado");
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar trabajador: " + e.getMessage());
        }
    }

    public java.util.List<Trabajador> obtenerTrabajadores() {
        java.util.List<Trabajador> lista = new java.util.ArrayList<>();
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trabajadores");
            while (rs.next()) {
                Trabajador t = new Trabajador(
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        rs.getString("telefono"),
                        rs.getString("direccion")
                );
                t.id = rs.getInt("id");
                lista.add(t);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return lista;
    }

    public void listarTrabajadores() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM trabajadores");
            System.out.println("\n👷 Trabajadores registrados:");
            System.out.println("================================");
            while (rs.next()) {
                System.out.println("  ID: " + rs.getInt("id") +
                        " | Nombre: " + rs.getString("nombre") +
                        " | Cédula: " + rs.getString("cedula") +
                        " | Tel: " + rs.getString("telefono") +
                        " | Dirección: " + rs.getString("direccion"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error al listar trabajadores: " + e.getMessage());
        }
    }

    // =========================================
    // JORNADAS
    // =========================================
    public void guardarJornada(Jornada j) {
        try {
            PreparedStatement check = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM jornadas WHERE trabajador_id=? " +
                            "AND lote_id=? AND fecha=? AND tipo_trabajo=?"
            );
            check.setInt(1, j.trabajadorId);
            check.setInt(2, j.loteId);
            check.setString(3, j.fecha);
            check.setString(4, j.tipoTrabajo);
            ResultSet rs = check.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("⚠️ Jornada de " + j.trabajadorId + " ya existe, omitiendo...");
                return;
            }
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO jornadas (trabajador_id, lote_id, fecha, tipo_trabajo, " +
                            "modo_pago, kilos, valor_dia, valor_kilo, total_pagar, observaciones) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setInt(1, j.trabajadorId);
            ps.setInt(2, j.loteId);
            ps.setString(3, j.fecha);
            ps.setString(4, j.tipoTrabajo);
            ps.setString(5, j.modoPago);
            ps.setDouble(6, j.kilos);
            ps.setDouble(7, j.valorDia);
            ps.setDouble(8, j.valorKilo);
            ps.setDouble(9, j.totalPagar);
            ps.setString(10, j.observaciones);
            ps.executeUpdate();
            System.out.println("✅ Jornada guardada | Total a pagar: $" + j.totalPagar);
        } catch (Exception e) {
            System.out.println("❌ Error al guardar jornada: " + e.getMessage());
        }
    }

    public java.util.List<Jornada> obtenerJornadas() {
        java.util.List<Jornada> lista = new java.util.ArrayList<>();
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT j.id, t.nombre as trab, l.nombre as lote, " +
                            "j.fecha, j.tipo_trabajo, j.modo_pago, " +
                            "j.kilos, j.valor_dia, j.valor_kilo, " +
                            "j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN trabajadores t ON j.trabajador_id = t.id " +
                            "JOIN lotes l ON j.lote_id = l.id " +
                            "ORDER BY j.fecha DESC"
            );
            while (rs.next()) {
                Jornada j = new Jornada(
                        0, 0,
                        rs.getString("fecha"),
                        rs.getString("tipo_trabajo"),
                        rs.getString("modo_pago"),
                        rs.getDouble("kilos"),
                        rs.getDouble("valor_dia"),
                        rs.getDouble("valor_kilo"),
                        rs.getString("observaciones")
                );
                j.id               = rs.getInt("id");
                j.nombreTrabajador = rs.getString("trab");
                j.nombreLote       = rs.getString("lote");
                lista.add(j);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return lista;
    }

    public void listarJornadas() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT j.id, t.nombre, l.nombre as lote, j.fecha, " +
                            "j.tipo_trabajo, j.modo_pago, j.kilos, j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN trabajadores t ON j.trabajador_id = t.id " +
                            "JOIN lotes l ON j.lote_id = l.id"
            );
            System.out.println("\n📋 Jornadas registradas:");
            System.out.println("================================");
            while (rs.next()) {
                System.out.println("  " + rs.getString("nombre") +
                        " | Lote: " + rs.getString("lote") +
                        " | Fecha: " + rs.getString("fecha") +
                        " | Labor: " + rs.getString("tipo_trabajo") +
                        " | Pago: " + rs.getString("modo_pago") +
                        " | Kilos: " + rs.getDouble("kilos") +
                        " | Total: $" + rs.getDouble("total_pagar") +
                        " | Obs: " + rs.getString("observaciones"));
            }
        } catch (Exception e) {
            System.out.println("❌ Error al listar jornadas: " + e.getMessage());
        }
    }

    // =========================================
    // INVENTARIO
    // =========================================
    public void guardarProducto(Producto p) {
        try {
            PreparedStatement check = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM productos_bodega WHERE nombre=? AND activo=1"
            );
            check.setString(1, p.nombre);
            ResultSet rs = check.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("⚠️ Producto '" + p.nombre + "' ya existe, omitiendo...");
                return;
            }
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO productos_bodega (nombre, tipo, unidad_medida, " +
                            "stock_actual, stock_minimo, precio_unidad, activo) VALUES (?, ?, ?, ?, ?, ?, 1)"
            );
            ps.setString(1, p.nombre);
            ps.setString(2, p.tipo);
            ps.setString(3, p.unidadMedida);
            ps.setDouble(4, p.stockActual);
            ps.setDouble(5, p.stockMinimo);
            ps.setDouble(6, p.precioUnidad);
            ps.executeUpdate();
            System.out.println("✅ Producto '" + p.nombre + "' guardado");
        } catch (Exception e) {
            System.out.println("❌ Error al guardar producto: " + e.getMessage());
        }
    }

    public void registrarMovimiento(int productoId, int loteId, String tipo,
                                    double cantidad, String fecha, String observaciones) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO movimientos_bodega (producto_id, lote_id, tipo_movimiento, " +
                            "cantidad, fecha, observaciones) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setInt(1, productoId);
            ps.setInt(2, loteId);
            ps.setString(3, tipo);
            ps.setDouble(4, cantidad);
            ps.setString(5, fecha);
            ps.setString(6, observaciones);
            ps.executeUpdate();
            String operacion = tipo.equals("entrada") ? "+" : "-";
            PreparedStatement upd = conexion.prepareStatement(
                    "UPDATE productos_bodega SET stock_actual = stock_actual " + operacion + " ? WHERE id=?"
            );
            upd.setDouble(1, cantidad);
            upd.setInt(2, productoId);
            upd.executeUpdate();
            System.out.println("✅ Movimiento '" + tipo + "' registrado | Cantidad: " + cantidad);
            verificarStockMinimo(productoId);
        } catch (Exception e) {
            System.out.println("❌ Error al registrar movimiento: " + e.getMessage());
        }
    }

    public void verificarStockMinimo(int productoId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT nombre, stock_actual, stock_minimo FROM productos_bodega WHERE id=?"
            );
            ps.setInt(1, productoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double actual = rs.getDouble("stock_actual");
                double minimo = rs.getDouble("stock_minimo");
                String nombre = rs.getString("nombre");
                if (actual <= minimo) {
                    System.out.println("🚨 ALERTA: '" + nombre +
                            "' tiene stock bajo! Actual: " + actual +
                            " | Mínimo: " + minimo);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error al verificar stock: " + e.getMessage());
        }
    }

    public void eliminarProducto(int id) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE productos_bodega SET activo=0 WHERE id=?"
            );
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Producto eliminado correctamente");
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar producto: " + e.getMessage());
        }
    }

    public void listarProductos() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM productos_bodega WHERE activo=1");
            System.out.println("\n📦 Inventario bodega:");
            System.out.println("================================");
            while (rs.next()) {
                String alerta = rs.getDouble("stock_actual") <= rs.getDouble("stock_minimo")
                        ? " ⚠️ STOCK BAJO" : "";
                System.out.println("  ID: " + rs.getInt("id") +
                        " | " + rs.getString("nombre") +
                        " | Tipo: " + rs.getString("tipo") +
                        " | Stock: " + rs.getDouble("stock_actual") + " " + rs.getString("unidad_medida") +
                        " | Mínimo: " + rs.getDouble("stock_minimo") +
                        " | Precio: $" + rs.getDouble("precio_unidad") + alerta);
            }
        } catch (Exception e) {
            System.out.println("❌ Error al listar productos: " + e.getMessage());
        }
    }

    // =========================================
    // TIQUETES
    // =========================================
    public int siguienteNumeroTiquete() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tiquetes");
            return rs.getInt(1) + 1;
        } catch (Exception e) {
            System.out.println("❌ Error al obtener numero tiquete: " + e.getMessage());
            return 1;
        }
    }

    public void generarTiquete(int jornadaId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT t.nombre, t.cedula, l.nombre as lote, " +
                            "j.fecha, j.tipo_trabajo, j.modo_pago, " +
                            "j.kilos, j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN trabajadores t ON j.trabajador_id = t.id " +
                            "JOIN lotes l ON j.lote_id = l.id " +
                            "WHERE j.id=?"
            );
            ps.setInt(1, jornadaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int numero = siguienteNumeroTiquete();
                Tiquete tiquete = new Tiquete(
                        numero,
                        rs.getString("nombre"),
                        rs.getString("cedula"),
                        rs.getString("lote"),
                        rs.getString("fecha"),
                        rs.getString("tipo_trabajo"),
                        rs.getString("modo_pago"),
                        rs.getDouble("kilos"),
                        rs.getDouble("total_pagar"),
                        rs.getString("observaciones")
                );
                PreparedStatement ins = conexion.prepareStatement(
                        "INSERT INTO tiquetes (numero_tiquete, jornada_id, " +
                                "trabajador_id, fecha_pago, total_pagado, impreso) VALUES (?, ?, ?, ?, ?, 1)"
                );
                ins.setInt(1, numero);
                ins.setInt(2, jornadaId);
                ins.setInt(3, 1);
                ins.setString(4, rs.getString("fecha"));
                ins.setDouble(5, rs.getDouble("total_pagar"));
                ins.executeUpdate();
                System.out.println("\n" + tiquete.generarTiquete());
            }
        } catch (Exception e) {
            System.out.println("❌ Error al generar tiquete: " + e.getMessage());
        }
    }

    // =========================================
    // COSECHA
    // =========================================
    public int guardarCosecha(Cosecha c) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO cosecha (nombre, fecha_inicio, fecha_fin, estado, " +
                            "total_cereza_kg, estimado_pergamino_kg, pergamino_real_kg, " +
                            "precio_kilo_venta, total_venta, gastos_trabajadores, ganancia_neta, observaciones) " +
                            "VALUES (?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0, ?)"
            );
            ps.setString(1, c.nombre);
            ps.setString(2, c.fechaInicio);
            ps.setString(3, c.fechaFin);
            ps.setString(4, c.estado);
            ps.setString(5, c.observaciones);
            ps.executeUpdate();
            ResultSet rs = conexion.createStatement().executeQuery("SELECT last_insert_rowid()");
            int id = rs.getInt(1);
            System.out.println("✅ Cosecha '" + c.nombre + "' iniciada con ID: " + id);
            return id;
        } catch (Exception e) {
            System.out.println("❌ Error al guardar cosecha: " + e.getMessage());
            return -1;
        }
    }

    public void guardarCosechaLote(CosechaLote cl) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO cosecha_lotes (cosecha_id, lote_id, fecha_inicio, " +
                            "fecha_fin, estado, total_cereza_kg, estimado_pergamino_kg, " +
                            "pergamino_real_kg, observaciones) VALUES (?, ?, ?, ?, ?, 0, 0, 0, ?)"
            );
            ps.setInt(1, cl.cosechaId);
            ps.setInt(2, cl.loteId);
            ps.setString(3, cl.fechaInicio);
            ps.setString(4, cl.fechaFin);
            ps.setString(5, cl.estado);
            ps.setString(6, cl.observaciones);
            ps.executeUpdate();
            System.out.println("✅ Lote ID:" + cl.loteId + " agregado a cosecha");
        } catch (Exception e) {
            System.out.println("❌ Error al guardar lote cosecha: " + e.getMessage());
        }
    }

    public void actualizarCerezaLote(int cosechaLoteId, double kilosNuevos) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE cosecha_lotes SET " +
                            "total_cereza_kg = total_cereza_kg + ?, " +
                            "estimado_pergamino_kg = (total_cereza_kg + ?) * " +
                            CosechaLote.PROPORCION_PERGAMINO + " WHERE id=?"
            );
            ps.setDouble(1, kilosNuevos);
            ps.setDouble(2, kilosNuevos);
            ps.setInt(3, cosechaLoteId);
            ps.executeUpdate();
            System.out.println("✅ Cereza actualizada: +" + kilosNuevos + " kg");
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar cereza: " + e.getMessage());
        }
    }

    public void cambiarEstadoLote(int cosechaLoteId, String nuevoEstado, String fechaFin) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE cosecha_lotes SET estado=?, fecha_fin=? WHERE id=?"
            );
            ps.setString(1, nuevoEstado);
            ps.setString(2, fechaFin);
            ps.setInt(3, cosechaLoteId);
            ps.executeUpdate();
            System.out.println("✅ Estado lote actualizado a: " + nuevoEstado);
        } catch (Exception e) {
            System.out.println("❌ Error al cambiar estado: " + e.getMessage());
        }
    }

    public void verResumenCosecha(int cosechaId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT c.nombre, c.estado, c.fecha_inicio, c.fecha_fin, " +
                            "SUM(cl.total_cereza_kg) as total_cereza, " +
                            "SUM(cl.estimado_pergamino_kg) as total_pergamino " +
                            "FROM cosecha c " +
                            "JOIN cosecha_lotes cl ON c.id = cl.cosecha_id " +
                            "WHERE c.id=?"
            );
            ps.setInt(1, cosechaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n☕ RESUMEN COSECHA: " + rs.getString("nombre"));
                System.out.println("================================");
                System.out.println("Estado:         " + rs.getString("estado"));
                System.out.println("Fecha inicio:   " + rs.getString("fecha_inicio"));
                System.out.println("Fecha fin:      " + (rs.getString("fecha_fin") != null ? rs.getString("fecha_fin") : "En curso"));
                System.out.println("Total cereza:   " + rs.getDouble("total_cereza") + " kg");
                System.out.println("Est. pergamino: " + String.format("%.1f", rs.getDouble("total_pergamino")) + " kg");
            }
            PreparedStatement ps2 = conexion.prepareStatement(
                    "SELECT l.nombre as lote, cl.estado, cl.total_cereza_kg, cl.estimado_pergamino_kg " +
                            "FROM cosecha_lotes cl " +
                            "JOIN lotes l ON cl.lote_id = l.id " +
                            "WHERE cl.cosecha_id=?"
            );
            ps2.setInt(1, cosechaId);
            ResultSet rs2 = ps2.executeQuery();
            System.out.println("--------------------------------");
            System.out.println("Detalle por lote:");
            while (rs2.next()) {
                String emoji = rs2.getString("estado").equals("terminado") ? "🟢" :
                        rs2.getString("estado").equals("pendiente") ? "🔴" : "🟡";
                System.out.println("  " + emoji + " " + rs2.getString("lote") +
                        " | Cereza: " + rs2.getDouble("total_cereza_kg") + " kg" +
                        " | Pergamino est.: " + String.format("%.1f", rs2.getDouble("estimado_pergamino_kg")) + " kg" +
                        " | Estado: " + rs2.getString("estado"));
            }
            System.out.println("================================");
        } catch (Exception e) {
            System.out.println("❌ Error al ver resumen: " + e.getMessage());
        }
    }

    // =========================================
    // REPORTES / HISTORIALES
    // =========================================
    public void historialPorLote(int loteId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT t.nombre as trabajador, j.fecha, j.tipo_trabajo, " +
                            "j.modo_pago, j.kilos, j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN trabajadores t ON j.trabajador_id = t.id " +
                            "WHERE j.lote_id=? ORDER BY j.fecha ASC"
            );
            ps.setInt(1, loteId);
            ResultSet rs = ps.executeQuery();
            PreparedStatement psLote = conexion.prepareStatement(
                    "SELECT nombre FROM lotes WHERE id=?"
            );
            psLote.setInt(1, loteId);
            ResultSet rsLote = psLote.executeQuery();
            String nombreLote = rsLote.next() ? rsLote.getString("nombre") : "Lote " + loteId;
            System.out.println("\n🌱 HISTORIAL LOTE: " + nombreLote);
            System.out.println("================================");
            double totalPagado = 0, totalKilos = 0;
            int totalDias = 0;
            while (rs.next()) {
                String pago = rs.getString("modo_pago").equals("kilo") ?
                        rs.getDouble("kilos") + " kg" : "Por día";
                System.out.println("  " + rs.getString("fecha") +
                        " | " + rs.getString("trabajador") +
                        " | Labor: " + rs.getString("tipo_trabajo") +
                        " | " + pago +
                        " | $" + String.format("%,.0f", rs.getDouble("total_pagar")) +
                        " | Obs: " + rs.getString("observaciones"));
                totalPagado += rs.getDouble("total_pagar");
                totalKilos  += rs.getDouble("kilos");
                totalDias++;
            }
            System.out.println("--------------------------------");
            System.out.println("Total jornadas: " + totalDias);
            System.out.println("Total kilos:    " + totalKilos + " kg");
            System.out.println("Total pagado:   $" + String.format("%,.0f", totalPagado));
            System.out.println("================================");
        } catch (Exception e) {
            System.out.println("❌ Error historial lote: " + e.getMessage());
        }
    }

    public void historialPorTrabajador(int trabajadorId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT l.nombre as lote, j.fecha, j.tipo_trabajo, " +
                            "j.modo_pago, j.kilos, j.total_pagar, j.observaciones " +
                            "FROM jornadas j " +
                            "JOIN lotes l ON j.lote_id = l.id " +
                            "WHERE j.trabajador_id=? ORDER BY j.fecha ASC"
            );
            ps.setInt(1, trabajadorId);
            ResultSet rs = ps.executeQuery();
            PreparedStatement psTrab = conexion.prepareStatement(
                    "SELECT nombre FROM trabajadores WHERE id=?"
            );
            psTrab.setInt(1, trabajadorId);
            ResultSet rsTrab = psTrab.executeQuery();
            String nombreTrab = rsTrab.next() ? rsTrab.getString("nombre") : "Trabajador " + trabajadorId;
            System.out.println("\n👷 HISTORIAL TRABAJADOR: " + nombreTrab);
            System.out.println("================================");
            double totalPagado = 0, totalKilos = 0;
            int totalDias = 0;
            while (rs.next()) {
                String pago = rs.getString("modo_pago").equals("kilo") ?
                        rs.getDouble("kilos") + " kg" : "Por día";
                System.out.println("  " + rs.getString("fecha") +
                        " | Lote: " + rs.getString("lote") +
                        " | Labor: " + rs.getString("tipo_trabajo") +
                        " | " + pago +
                        " | $" + String.format("%,.0f", rs.getDouble("total_pagar")) +
                        " | Obs: " + rs.getString("observaciones"));
                totalPagado += rs.getDouble("total_pagar");
                totalKilos  += rs.getDouble("kilos");
                totalDias++;
            }
            System.out.println("--------------------------------");
            System.out.println("Total jornadas:  " + totalDias);
            System.out.println("Total kilos rec: " + totalKilos + " kg");
            System.out.println("Total pagado:    $" + String.format("%,.0f", totalPagado));
            System.out.println("================================");
        } catch (Exception e) {
            System.out.println("❌ Error historial trabajador: " + e.getMessage());
        }
    }

    // =========================================
    // USUARIOS
    // =========================================
    public void guardarUsuario(Usuario u) {
        try {
            PreparedStatement check = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM usuarios WHERE usuario=?"
            );
            check.setString(1, u.usuario);
            ResultSet rs = check.executeQuery();
            if (rs.getInt(1) > 0) {
                System.out.println("⚠️ Usuario '" + u.usuario + "' ya existe, omitiendo...");
                return;
            }
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO usuarios (nombre, usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?, 1)"
            );
            ps.setString(1, u.nombre);
            ps.setString(2, u.usuario);
            ps.setString(3, u.contrasena);
            ps.setString(4, u.rol);
            ps.executeUpdate();
            System.out.println("✅ Usuario '" + u.usuario + "' creado correctamente");
        } catch (Exception e) {
            System.out.println("❌ Error al guardar usuario: " + e.getMessage());
        }
    }

    public Usuario login(String usuarioIngresado, String contrasenaIngresada) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT * FROM usuarios WHERE usuario=? AND activo=1"
            );
            ps.setString(1, usuarioIngresado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashGuardado = rs.getString("contrasena");
                if (Usuario.verificarContrasena(contrasenaIngresada, hashGuardado)) {
                    System.out.println("✅ Login exitoso | Bienvenido " +
                            rs.getString("nombre") + " | Rol: " + rs.getString("rol"));
                    Usuario u = new Usuario(
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            contrasenaIngresada,
                            rs.getString("rol")
                    );
                    u.id = rs.getInt("id");
                    return u;
                } else {
                    System.out.println("❌ Contraseña incorrecta");
                    return null;
                }
            } else {
                System.out.println("❌ Usuario no encontrado");
                return null;
            }
        } catch (Exception e) {
            System.out.println("❌ Error al hacer login: " + e.getMessage());
            return null;
        }
    }

    // =========================================
    // CHAT IA
    // =========================================
    public void guardarChatHistorial(int usuarioId, String pregunta, String respuesta) {
        try {
            String fecha = java.time.LocalDate.now().toString();
            String hora  = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO chat_historial (usuario_id, pregunta, respuesta, fecha, hora) " +
                            "VALUES (?, ?, ?, ?, ?)"
            );
            ps.setInt(1, usuarioId);
            ps.setString(2, pregunta);
            ps.setString(3, respuesta);
            ps.setString(4, fecha);
            ps.setString(5, hora);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Error al guardar historial: " + e.getMessage());
        }
    }

    public void listarChatHistorial(int usuarioId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT ch.fecha, ch.hora, ch.pregunta, ch.respuesta, u.nombre as usuario " +
                            "FROM chat_historial ch " +
                            "JOIN usuarios u ON ch.usuario_id = u.id " +
                            "WHERE ch.usuario_id=? " +
                            "ORDER BY ch.fecha DESC, ch.hora DESC"
            );
            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n💬 HISTORIAL CHAT IA:");
            System.out.println("================================");
            while (rs.next()) {
                System.out.println("\n📅 " + rs.getString("fecha") +
                        " 🕐 " + rs.getString("hora") +
                        " | " + rs.getString("usuario"));
                System.out.println("❓ " + rs.getString("pregunta"));
                System.out.println("🤖 " + rs.getString("respuesta")
                        .substring(0, Math.min(rs.getString("respuesta").length(), 100)) + "...");
                System.out.println("--------------------------------");
            }
        } catch (Exception e) {
            System.out.println("❌ Error al listar historial: " + e.getMessage());
        }
    }

    // =========================================
    // CONTEXTO COMPLETO PARA LA IA
    // =========================================
    public String obtenerResumenLotes() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT l.nombre, l.matas, COALESCE(SUM(j.kilos), 0) as total_kilos " +
                            "FROM lotes l " +
                            "LEFT JOIN jornadas j ON l.id = j.lote_id AND j.tipo_trabajo='recoleccion' " +
                            "GROUP BY l.id, l.nombre, l.matas"
            );
            StringBuilder resumen = new StringBuilder();
            while (rs.next()) {
                resumen.append("Lote: ").append(rs.getString("nombre"))
                        .append(" | Matas: ").append(rs.getInt("matas"))
                        .append(" | Kilos recogidos: ").append(rs.getDouble("total_kilos"))
                        .append("\n");
            }
            return resumen.toString();
        } catch (Exception e) {
            return "Error al obtener lotes: " + e.getMessage();
        }
    }

    public String obtenerProductosBajoStock() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT nombre, tipo, stock_actual, stock_minimo, unidad_medida " +
                            "FROM productos_bodega WHERE activo=1"
            );
            StringBuilder resumen = new StringBuilder();
            while (rs.next()) {
                String alerta = rs.getDouble("stock_actual") <= rs.getDouble("stock_minimo")
                        ? " ⚠️ STOCK BAJO" : "";
                resumen.append("Producto: ").append(rs.getString("nombre"))
                        .append(" | Tipo: ").append(rs.getString("tipo"))
                        .append(" | Stock: ").append(rs.getDouble("stock_actual"))
                        .append(" ").append(rs.getString("unidad_medida"))
                        .append(" | Mínimo: ").append(rs.getDouble("stock_minimo"))
                        .append(alerta).append("\n");
            }
            return resumen.toString();
        } catch (Exception e) {
            return "Error al obtener productos: " + e.getMessage();
        }
    }

    public String obtenerResumenTrabajadores() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT t.nombre, COUNT(j.id) as total_jornadas, " +
                            "COALESCE(SUM(j.kilos), 0) as total_kilos, " +
                            "COALESCE(SUM(j.total_pagar), 0) as total_pagado " +
                            "FROM trabajadores t " +
                            "LEFT JOIN jornadas j ON t.id = j.trabajador_id " +
                            "GROUP BY t.id, t.nombre ORDER BY total_jornadas DESC"
            );
            StringBuilder resumen = new StringBuilder();
            while (rs.next()) {
                resumen.append("Trabajador: ").append(rs.getString("nombre"))
                        .append(" | Jornadas: ").append(rs.getInt("total_jornadas"))
                        .append(" | Kilos rec: ").append(rs.getDouble("total_kilos"))
                        .append(" | Total pagado: $")
                        .append(String.format("%,.0f", rs.getDouble("total_pagado")))
                        .append("\n");
            }
            return resumen.toString();
        } catch (Exception e) {
            return "Error al obtener trabajadores: " + e.getMessage();
        }
    }

    public String obtenerGananciaCosecha() {
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT c.nombre, c.estado, " +
                            "SUM(cl.total_cereza_kg) as total_cereza, " +
                            "SUM(cl.estimado_pergamino_kg) as total_pergamino " +
                            "FROM cosecha c " +
                            "JOIN cosecha_lotes cl ON c.id = cl.cosecha_id " +
                            "GROUP BY c.id, c.nombre, c.estado"
            );
            StringBuilder resumen = new StringBuilder();
            while (rs.next()) {
                resumen.append("Cosecha: ").append(rs.getString("nombre"))
                        .append(" | Estado: ").append(rs.getString("estado"))
                        .append(" | Cereza: ").append(rs.getDouble("total_cereza")).append(" kg")
                        .append(" | Est. Pergamino: ")
                        .append(String.format("%.1f", rs.getDouble("total_pergamino"))).append(" kg\n");
            }
            ResultSet rs2 = st.executeQuery(
                    "SELECT COALESCE(SUM(total_pagar), 0) as total_gastos FROM jornadas"
            );
            if (rs2.next()) {
                resumen.append("Total gastos trabajadores: $")
                        .append(String.format("%,.0f", rs2.getDouble("total_gastos"))).append("\n");
            }
            return resumen.toString();
        } catch (Exception e) {
            return "Error al obtener cosecha: " + e.getMessage();
        }
    }

    public String obtenerContextoCompletoIA() {
        StringBuilder contexto = new StringBuilder();
        contexto.append("================================\n");
        contexto.append("DATOS ACTUALES FINCA LA QUINTA\n");
        contexto.append("================================\n\n");
        contexto.append("🌱 LOTES:\n--------------------------------\n");
        contexto.append(obtenerResumenLotes()).append("\n");
        contexto.append("📦 INVENTARIO:\n--------------------------------\n");
        contexto.append(obtenerProductosBajoStock()).append("\n");
        contexto.append("👷 TRABAJADORES:\n--------------------------------\n");
        contexto.append(obtenerResumenTrabajadores()).append("\n");
        contexto.append("☕ COSECHA:\n--------------------------------\n");
        contexto.append(obtenerGananciaCosecha()).append("\n");
        contexto.append("================================\n");
        return contexto.toString();
    }

    public String obtenerContextoReducidoIA() {
        StringBuilder ctx = new StringBuilder();
        ctx.append("FINCA LA QUINTA — Datos clave:\n");
        try {
            // Solo cosecha activa
            ResultSet rs = conexion.createStatement().executeQuery(
                    "SELECT nombre, estado FROM cosecha " +
                            "WHERE estado='en_proceso' LIMIT 1"
            );
            if (rs.next()) {
                ctx.append("Cosecha activa: ")
                        .append(rs.getString("nombre")).append("\n");
            }

            // Solo stock bajo
            ResultSet rs2 = conexion.createStatement().executeQuery(
                    "SELECT nombre, stock_actual, stock_minimo " +
                            "FROM productos_bodega " +
                            "WHERE stock_actual <= stock_minimo AND activo=1"
            );
            ctx.append("Stock bajo: ");
            boolean hayStock = false;
            while (rs2.next()) {
                ctx.append(rs2.getString("nombre")).append(" ");
                hayStock = true;
            }
            if (!hayStock) ctx.append("ninguno");
            ctx.append("\n");

            // Pagos pendientes
            ResultSet rs3 = conexion.createStatement().executeQuery(
                    "SELECT t.nombre, " +
                            "COALESCE(SUM(j.total_pagar),0) as total " +
                            "FROM trabajadores t " +
                            "JOIN jornadas j ON t.id=j.trabajador_id " +
                            "WHERE j.id NOT IN " +
                            "(SELECT jornada_id FROM tiquetes) " +
                            "GROUP BY t.id"
            );
            ctx.append("Pagos pendientes:\n");
            while (rs3.next()) {
                ctx.append("- ").append(rs3.getString("nombre"))
                        .append(": $").append(
                                String.format("%,.0f",
                                        rs3.getDouble("total")))
                        .append("\n");
            }
        } catch (Exception e) {
            System.out.println("❌ Contexto reducido: " + e.getMessage());
        }
        return ctx.toString();
    }

    // Obtener lista de productos activos
    public java.util.List<Producto> obtenerProductos() {
        java.util.List<Producto> lista = new java.util.ArrayList<>();
        try {
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT * FROM productos_bodega WHERE activo=1"
            );
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getString("nombre"),
                        rs.getString("tipo"),
                        rs.getString("unidad_medida"),
                        rs.getDouble("stock_actual"),
                        rs.getDouble("stock_minimo"),
                        rs.getDouble("precio_unidad")
                );
                p.id = rs.getInt("id");
                lista.add(p);
            }
        } catch (Exception e) {
            System.out.println("❌ Error obteniendo productos: " + e.getMessage());
        }
        return lista;
    }

    // Actualizar producto existente
    public void actualizarProducto(int id, String nombre, String tipo,
                                   String unidad, double stock, double minimo, double precio) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE productos_bodega SET nombre=?, tipo=?, " +
                            "unidad_medida=?, stock_actual=?, stock_minimo=?, " +
                            "precio_unidad=? WHERE id=?"
            );
            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setString(3, unidad);
            ps.setDouble(4, stock);
            ps.setDouble(5, minimo);
            ps.setDouble(6, precio);
            ps.setInt(7, id);
            ps.executeUpdate();
            System.out.println("✅ Producto actualizado");
        } catch (Exception e) {
            System.out.println("❌ Error al actualizar producto: " + e.getMessage());
        }
    }

    // Movimiento de bodega sin lote asociado
    public void registrarMovimientoSinLote(int productoId, String tipo,
                                           double cantidad, String fecha, String observaciones) {
        try {
            // Guardar movimiento con lote_id NULL
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO movimientos_bodega (producto_id, lote_id, " +
                            "tipo_movimiento, cantidad, fecha, observaciones) " +
                            "VALUES (?, NULL, ?, ?, ?, ?)"
            );
            ps.setInt(1, productoId);
            ps.setString(2, tipo);
            ps.setDouble(3, cantidad);
            ps.setString(4, fecha);
            ps.setString(5, observaciones);
            ps.executeUpdate();

            // Actualizar stock
            String operacion = tipo.equals("entrada") ? "+" : "-";
            PreparedStatement upd = conexion.prepareStatement(
                    "UPDATE productos_bodega SET stock_actual = " +
                            "stock_actual " + operacion + " ? WHERE id=?"
            );
            upd.setDouble(1, cantidad);
            upd.setInt(2, productoId);
            upd.executeUpdate();

            System.out.println("✅ Movimiento '" + tipo +
                    "' registrado | Cantidad: " + cantidad);

            verificarStockMinimo(productoId);

        } catch (Exception e) {
            System.out.println("❌ Error al registrar movimiento: "
                    + e.getMessage());
        }
    }

    // Obtener cosecha activa (en_proceso o en_espera)
    public int obtenerCosechaActivaId() {
        try {
            ResultSet rs = conexion.createStatement().executeQuery(
                    "SELECT id FROM cosecha " +
                            "WHERE estado = 'en_proceso' " +
                            "ORDER BY fecha_inicio DESC LIMIT 1"
            );
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) {
            System.out.println("❌ Error cosecha activa: " + e.getMessage());
        }
        return -1;
    }

    // Obtener el id de cosecha_lotes dado cosechaId y loteId
    public int obtenerCosechaLoteId(int cosechaId, int loteId) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "SELECT id FROM cosecha_lotes " +
                            "WHERE cosecha_id=? AND lote_id=?"
            );
            ps.setInt(1, cosechaId);
            ps.setInt(2, loteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) {
            System.out.println("❌ Error cosecha lote id: " + e.getMessage());
        }
        return -1;
    }

    // =========================================
    // CONEXIÓN Y CIERRE
    // =========================================
    public java.sql.Connection getConexion() {
        return conexion;
    }

    public void cerrar() {
        try {
            conexion.close();
            System.out.println("✅ Conexión cerrada");
        } catch (Exception e) {
            System.out.println("❌ Error al cerrar: " + e.getMessage());
        }
    }
}
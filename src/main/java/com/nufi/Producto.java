package com.nufi;

public class Producto {

    public int    id;
    public String nombre;
    public String tipo;
    public String unidadMedida;
    public double stockActual;
    public double stockMinimo;
    public double precioUnidad;

    public Producto(String nombre, String tipo, String unidadMedida,
                    double stockActual, double stockMinimo, double precioUnidad) {
        this.nombre       = nombre;
        this.tipo         = tipo;
        this.unidadMedida = unidadMedida;
        this.stockActual  = stockActual;
        this.stockMinimo  = stockMinimo;
        this.precioUnidad = precioUnidad;
    }

    public int    getId()           { return id; }
    public String getNombre()       { return nombre; }
    public String getTipo()         { return tipo; }
    public String getUnidadMedida() { return unidadMedida; }
    public double getStockActual()  { return stockActual; }
    public double getStockMinimo()  { return stockMinimo; }
    public double getPrecioUnidad() { return precioUnidad; }
}
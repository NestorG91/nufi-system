package com.nufi;

public class Producto {

    int id;
    String nombre;
    String tipo; // abono, veneno, herramienta
    String unidadMedida; // kilos, bultos, litros
    double stockActual;
    double stockMinimo;
    double precioUnidad;
    int activo;

//Se crea el contructor
    public  Producto(String nombre, String tipo, String unidadMedida, double stockActual, double stockMinimo,
                     double precioUnidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.unidadMedida = unidadMedida;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.precioUnidad = precioUnidad;
        this.activo = 1; // siempre activo al crearse
    }
// Verificar si el stock está bajo
    public boolean stockBajo(){
        return stockActual <= stockMinimo;
    }
// Mostrar info del producto
    public void mostrarInfo(){
    String alerta = stockBajo() ? " ⚠️ STOCK BAJO" : "";
        System.out.println(" " + nombre +
                " | Tipo: " + tipo +
                " | Unidad: " + unidadMedida +
                " | Stock: " + stockActual +
                " | Mínimo: " + stockMinimo +
                " | Precio: " + precioUnidad +
                alerta);
    }
}

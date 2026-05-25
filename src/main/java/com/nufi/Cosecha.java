package com.nufi;

public class Cosecha {

    int id;
    String nombre;
    String fechaInicio;
    String fechaFin;
    String estado;
    double totalCerezaKg;
    double estimadoPergaminoKg;
    double pergaminoRealKg;
    double precioKiloVenta;
    double totalVenta;
    double gastosTrabajadores;
    double gananciaNeta;
    String observaciones;

// Proporción real de La Quinta
    private static final double PROPORCION_PERGAMINO = 0.198;

//Constructor
    public Cosecha(String nombre, String fechaInicio, String observaciones) {
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = null;
        this.estado = "en_proceso";
        this.observaciones = observaciones;
    }
// Calcular estimado de pergamino con proporción real
    public double calcularEstimadoPergamino(double cerezaKg) {
        return cerezaKg * PROPORCION_PERGAMINO;
    }
// Calcular ganancia neta
    public double calcularGanancia(){
        return totalVenta - gastosTrabajadores;
    }
// Mostrar resumen
    public void mostrarResumen(){
        System.out.println("\n☕ COSECHA: " + nombre);
        System.out.println("================================");
        System.out.println("Estado:              " + estado);
        System.out.println("Fecha inicio:        " + fechaInicio);
        System.out.println("Fecha fin:           " + (fechaFin != null ? fechaFin : "En curso"));
        System.out.println("Total cereza:        " + totalCerezaKg + " kg");
        System.out.println("Estimado pergamino:  " + estimadoPergaminoKg + " kg");
        System.out.println("Pergamino real:      " + pergaminoRealKg + " kg");
        System.out.println("Precio kilo:         $" + String.format("%,.0f", precioKiloVenta));
        System.out.println("Total venta:         $" + String.format("%,.0f", totalVenta));
        System.out.println("Gastos trabajadores: $" + String.format("%,.0f", gastosTrabajadores));
        System.out.println("Ganancia neta:       $" + String.format("%,.0f", gananciaNeta));
        System.out.println("================================");
    }
}

package com.nufi;

public class CosechaLote {

    int id;
    int cosechaId;
    int loteId;
    String fechaInicio;
    String fechaFin;
    String estado;
    double totalCerezaKg;
    double estimadoPergaminoKg;
    double pergaminoRealKg;
    String observaciones;

//Proporcion real de la Quinta
    public static final double PROPORCION_PERGAMINO = 0.198;

//Constructor
    public CosechaLote(int cosechaId, int loteId, String fechaInicio) {
        this.cosechaId   = cosechaId;
        this.loteId      = loteId;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = null;
        this.estado      = "en_proceso";
    }
// Calcular estimado pergamino
    public double calcularEstimado(double cerezaKg){
        return  cerezaKg * PROPORCION_PERGAMINO;
    }
// Mostrar info del lote en cosecha
    public void mostrarInfo() {
        System.out.println("  Lote ID: "           + loteId +
                " | Estado: "          + estado +
                " | Inicio: "          + fechaInicio +
                " | Fin: "             + (fechaFin != null ? fechaFin : "En curso") +
                " | Cereza: "          + totalCerezaKg + " kg" +
                " | Est. Pergamino: "  + estimadoPergaminoKg + " kg");
    }
}

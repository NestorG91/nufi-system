package com.nufi;

public class CosechaLote {

    // =========================================
    // ATRIBUTOS
    // =========================================
    public int    id;
    public int    cosechaId;
    public int    loteId;
    public String fechaInicio;
    public String fechaFin;
    public String estado;
    public double totalCerezaKg;
    public double estimadoPergaminoKg;
    public double pergaminoRealKg;
    public String observaciones;

    // Proporción real de La Quinta
    public static final double PROPORCION_PERGAMINO = 0.198;

    // =========================================
    // CONSTRUCTOR
    // =========================================
    public CosechaLote(int cosechaId, int loteId, String fechaInicio) {
        this.cosechaId   = cosechaId;
        this.loteId      = loteId;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = null;
        this.estado      = "en_proceso";
    }

    // =========================================
    // MÉTODOS
    // =========================================
    public double calcularEstimado(double cerezaKg) {
        return cerezaKg * PROPORCION_PERGAMINO;
    }

    public void mostrarInfo() {
        System.out.println("  Lote ID: "          + loteId +
                " | Estado: "         + estado +
                " | Inicio: "         + fechaInicio +
                " | Fin: "            + (fechaFin != null ? fechaFin : "En curso") +
                " | Cereza: "         + totalCerezaKg + " kg" +
                " | Est. Pergamino: " + estimadoPergaminoKg + " kg");
    }
}
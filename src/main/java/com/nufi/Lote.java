package com.nufi;

public class Lote {

    // =========================================
    // ATRIBUTOS
    // =========================================
    public int    id;
    public String nombre;
    public int    matas;
    public String fechaSiembra;
    public double kilosCosechados;

    // =========================================
    // CONSTRUCTOR
    // =========================================
    public Lote(String nombre, int matas, String fechaSiembra) {
        this.nombre       = nombre;
        this.matas        = matas;
        this.fechaSiembra = fechaSiembra;
    }

    // =========================================
    // GETTERS — necesarios para JavaFX TableView
    // =========================================
    public int    getId(){
        return id; }
    public String getNombre(){
        return nombre; }
    public int    getMatas(){
        return matas; }
    public String getFechaSiembra(){
        return fechaSiembra; }
    public double getKilosCosechados(){
        return kilosCosechados; }

    // =========================================
    // MOSTRAR INFO EN CONSOLA
    // =========================================
    public void mostrarInfo() {
        System.out.println("  Lote: " + nombre +
                " | Matas: "   + matas +
                " | Sembrado: " + fechaSiembra);
    }
}
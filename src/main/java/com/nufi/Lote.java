package com.nufi;

public class Lote {

    String nombre;
    int matas;
    String fechaSiembra;

    //Contructor

    public Lote(String nombre, int matas, String fechaSiembra) {
        this.nombre = nombre;
        this.matas = matas;
        this.fechaSiembra = fechaSiembra;
    }

    public void mostrarInfo(){
        System.out.println(" Lote: " + nombre + " | Matas: " + matas + " | Sembrado: " + fechaSiembra);
    }
}

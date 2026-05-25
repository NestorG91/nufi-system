package com.nufi;

public class Trabajador {

    int id;
    String nombre;
    String cedula;
    String telefono;
    String direccion;

    //Constructor
    public Trabajador(String nombre, String cedula, String telefono, String direccion){
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    //Mostrar informacion del trabajador
    public void mostrarInfo(){
        System.out.println(" Trabajador: " + nombre +
                " | Cédula: " + cedula +
                " | Telefono: " + telefono +
                " | Direccion: " + direccion );
    }
}


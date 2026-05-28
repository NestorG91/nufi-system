package com.nufi;

public class Trabajador {

    public int    id;
    public String nombre;
    public String cedula;
    public String telefono;
    public String direccion;
//Constructor
    public Trabajador(String nombre, String cedula,
                      String telefono, String direccion) {
        this.nombre    = nombre;
        this.cedula    = cedula;
        this.telefono  = telefono;
        this.direccion = direccion;
    }

    public int    getId()        { return id; }
    public String getNombre()    { return nombre; }
    public String getCedula()    { return cedula; }
    public String getTelefono()  { return telefono; }
    public String getDireccion() { return direccion; }
}


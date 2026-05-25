package com.nufi;

public class Tiquete {

    int numeroTiquete;
    String nombreTrabajador;
    String cedulaTrabajador;
    String nombreLote;
    String fecha;
    String tipoTrabajo;
    String modoPago;
    double kilos;
    double totalPagar;
    String observaciones;

//Constructor
    public Tiquete(int numeroTiquete, String nombreTrabajador, String cedulaTrabajador, String nombreLote, String fecha, String tipoTrabajo,
                   String modoPago, double kilos, double totalPagar, String observaciones) {
        this.numeroTiquete = numeroTiquete;
        this.nombreTrabajador = nombreTrabajador;
        this.cedulaTrabajador = cedulaTrabajador;
        this.nombreLote = nombreLote;
        this.fecha = fecha;
        this.tipoTrabajo = tipoTrabajo;
        this.modoPago = modoPago;
        this.kilos = kilos;
        this.totalPagar = totalPagar;
        this.observaciones = observaciones;
    }
// Generar el tiquete para imprimir
    public String generarTiquete(){
        String linea = "================================";
        String lineaCorta = "--------------------------------";
        String texto = "";

        texto += linea + "\n";
        texto += "      FINCA LA QUINTA         \n";
        texto += "       SISTEMA NUFI           \n";
        texto += "   Vereda Albania, Santander  \n";
        texto += linea + "\n";
        texto += "Tiquete N°: " + numeroTiquete + "\n";
        texto += "Fecha:      " + fecha + "\n";
        texto += lineaCorta + "\n";
        texto += "Trabajador: " + nombreTrabajador + "\n";
        texto += "Cedula:     " + cedulaTrabajador + "\n";
        texto += lineaCorta + "\n";
        texto += "Lote:       " + nombreLote + "\n";
        texto += "Labor:      " + tipoTrabajo + "\n";
        texto += "Modo pago:  " + modoPago + "\n";

        if (modoPago.equals("kilos")){
            texto += "kilos rec.: " + kilos + " kg\n";
        }
        texto += lineaCorta + "\n";
        texto += "TOTAL:      $" + String.format("%,.0f", totalPagar) + "\n";
        texto += linea + "\n";
        texto += "Observ.:    " + observaciones + "\n";
        texto += linea + "\n";
        texto += "Firma: ______________________\n";
        texto += linea + "\n";

        return texto;
    }
}

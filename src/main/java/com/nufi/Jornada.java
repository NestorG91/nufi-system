package com.nufi;

public class Jornada {

    public int    id;
    public String nombreTrabajador;
    public String nombreLote;

    int trabajadorId;
    int loteId;
    String fecha;
    String tipoTrabajo; // recoleccion, abono, siembra, guadaña
    String modoPago; // por dia, kilo
    double kilos;
    double valorDia;
    double valorKilo;
    double totalPagar;
    String observaciones;

    //Se crea el contructor

    public Jornada(int trabajadorId, int loteId, String fecha, String tipoTrabajo, String modoPago, double kilos,
                   double valorDia, double valorKilo, String observaciones) {

        this.trabajadorId = trabajadorId;
        this.loteId = loteId;
        this.fecha = fecha;
        this.tipoTrabajo = tipoTrabajo;
        this.modoPago = modoPago;
        this.kilos = kilos;
        this.valorDia = valorDia;
        this.valorKilo = valorKilo;
        this.observaciones = observaciones;

        //Calcular automaticamente
        if (modoPago.equals("kilo")){
            this.totalPagar = kilos * valorKilo;
        } else{
            this.totalPagar = valorDia;
        }
    }
    public int    getId(){
        return id; }
    public String getNombreTrabajador() {
        return nombreTrabajador; }
    public String getNombreLote()       {
        return nombreLote; }
    public String getFecha()            {
        return fecha; }
    public String getTipoTrabajo()      {
        return tipoTrabajo; }
    public String getModoPago()         {
        return modoPago; }
    public double getKilos()            {
        return kilos; }
    public double getTotalPagar()       {
        return totalPagar; }

    // Mostrar info de la jornada
    public void mostrarInfo() {
        System.out.println("  Trabajador ID: " + trabajadorId +
                " | Lote ID: "      + loteId +
                " | Fecha: "        + fecha +
                " | Labor: "        + tipoTrabajo +
                " | Pago: "         + modoPago +
                " | Total: $"       + totalPagar +
                " | Obs: "          + observaciones);
    }
}


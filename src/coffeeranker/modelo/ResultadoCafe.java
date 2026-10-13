package coffeeranker.modelo;

public class ResultadoCafe {
    private Cafe cafe;
    private Finca finca;
    private double promedio;
    private int numCalificaciones;
    private int posicion;

    public ResultadoCafe(Cafe cafe, Finca finca, double promedio, int numCalificaciones) {
        this.cafe = cafe;
        this.finca = finca;
        this.promedio = promedio;
        this.numCalificaciones = numCalificaciones;
    }

    public Cafe getCafe() { return cafe; }
    public Finca getFinca() { return finca; }
    public double getPromedio() { return promedio; }
    public int getNumCalificaciones() { return numCalificaciones; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }
}

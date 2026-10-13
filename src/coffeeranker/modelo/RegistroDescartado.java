package coffeeranker.modelo;

public class RegistroDescartado {
    private String archivo;
    private int numeroLinea;
    private String contenido;
    private String motivo;

    public RegistroDescartado(String archivo, int numeroLinea, String contenido, String motivo) {
        this.archivo = archivo;
        this.numeroLinea = numeroLinea;
        this.contenido = contenido;
        this.motivo = motivo;
    }

    public String getArchivo() { return archivo; }
    public int getNumeroLinea() { return numeroLinea; }
    public String getContenido() { return contenido; }
    public String getMotivo() { return motivo; }
}

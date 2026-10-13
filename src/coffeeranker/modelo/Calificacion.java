package coffeeranker.modelo;

public class Calificacion {
    private String idUsuario;
    private String idCafe;
    private int estrellas;
    private String archivoOrigen;

    public Calificacion(String idUsuario, String idCafe, int estrellas, String archivoOrigen) {
        this.idUsuario = idUsuario;
        this.idCafe = idCafe;
        this.estrellas = estrellas;
        this.archivoOrigen = archivoOrigen;
    }

    public String getIdUsuario() { return idUsuario; }
    public String getIdCafe() { return idCafe; }
    public int getEstrellas() { return estrellas; }
    public String getArchivoOrigen() { return archivoOrigen; }
}

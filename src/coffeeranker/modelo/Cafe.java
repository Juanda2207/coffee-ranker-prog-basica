package coffeeranker.modelo;

public class Cafe {
    private String id;
    private String nombre;
    private String categoria;
    private String idFinca;

    public Cafe(String id, String nombre, String categoria, String idFinca) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.idFinca = idFinca;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getIdFinca() { return idFinca; }

    @Override
    public String toString() {
        return id + ";" + nombre + ";" + categoria + ";" + idFinca;
    }
}

package coffeeranker.modelo;

public class Finca {
    private String id;
    private String nombre;
    private String departamento;

    public Finca(String id, String nombre, String departamento) {
        this.id = id;
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDepartamento() { return departamento; }

    @Override
    public String toString() {
        return id + ";" + nombre + ";" + departamento;
    }
}

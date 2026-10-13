package coffeeranker.generacion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class GeneradorDatos {

    private static final String[] VARIEDADES = {
        "Caturra", "Bourbon Rosado", "Geisha", "Castillo",
        "Típica", "Tabi", "Colombia", "Pink Bourbon"
    };
    private static final String[] PROCESOS = {"Lavado", "Natural", "Honey", "Anaeróbico"};
    private static final String[] FINCAS_NOMBRES = {
        "La Estrella", "La Esmeralda", "El Paraíso", "Monteblanco",
        "El Diviso", "La Palma", "Buenos Aires", "San Isidro"
    };
    private static final String[] DEPARTAMENTOS = {
        "Huila", "Nariño", "Quindío", "Tolima",
        "Antioquia", "Cauca", "Caldas", "Santander"
    };

    private static final String[] NOMBRES_USUARIOS = {
        "Ana Torres", "Luis García", "María López", "Carlos Rodríguez", "Sofía Martínez",
        "Diego Hernández", "Valentina Gómez", "Andrés Díaz", "Isabella Morales", "Felipe Jiménez",
        "Camila Vargas", "Sebastián Castro", "Daniela Ruiz", "Santiago Romero", "Gabriela Cruz",
        "Mateo Ramos", "Natalia Mendoza", "Juan Restrepo", "Paula Ríos", "Alejandro Moreno",
        "Laura Peña", "David Suárez", "Valeria Aguilar", "Julián Pardo", "Sara Medina",
        "Samuel Guerrero", "Mariana Flores", "Roberto Vargas", "Emilia Sánchez", "Tomás Navarro",
        "Lucía Campos", "Nicolás Ortiz", "Renata Vega", "Miguel Ibáñez", "Catalina Solano",
        "Esteban Acosta", "Nadia Molina", "Cristian Herrera", "Elena Cardona", "Javier Quintero"
    };

    private static final int MIN_CALIF_POR_USUARIO = 5;
    private static final int MAX_CALIF_POR_USUARIO = 15;
    private static final double PORCENTAJE_CORRUPTOS = 0.03;

    private final Random random;
    private final String carpetaEntrada;

    public GeneradorDatos(String carpetaEntrada, Long semilla) {
        this.carpetaEntrada = carpetaEntrada;
        this.random = semilla != null ? new Random(semilla) : new Random();
    }

    public void generar(int numFincas, int numCafes, int numUsuarios) throws IOException {
        Files.createDirectories(Paths.get(carpetaEntrada));

        List<String[]> fincas = generarFincas(numFincas);
        List<String[]> cafes = generarCafes(numCafes, fincas);
        List<String[]> usuarios = generarUsuarios(numUsuarios);

        escribirArchivo("fincas.csv", fincas);
        escribirArchivo("cafes.csv", cafes);
        escribirArchivo("usuarios.csv", usuarios);
        generarCalificaciones(usuarios, cafes);
    }

    private List<String[]> generarFincas(int numFincas) {
        int n = Math.min(numFincas, FINCAS_NOMBRES.length);
        List<String[]> fincas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            fincas.add(new String[]{
                String.format("F%03d", i + 1),
                FINCAS_NOMBRES[i],
                DEPARTAMENTOS[i]
            });
        }
        return fincas;
    }

    private List<String[]> generarCafes(int numCafes, List<String[]> fincas) {
        List<String[]> cafes = new ArrayList<>();
        for (int i = 0; i < numCafes; i++) {
            String variedad = VARIEDADES[random.nextInt(VARIEDADES.length)];
            String proceso = PROCESOS[random.nextInt(PROCESOS.length)];
            String[] finca = fincas.get(random.nextInt(fincas.size()));
            cafes.add(new String[]{
                String.format("C%03d", i + 1),
                variedad + " " + proceso,
                proceso,
                finca[0]
            });
        }
        return cafes;
    }

    private List<String[]> generarUsuarios(int numUsuarios) {
        int n = Math.min(numUsuarios, NOMBRES_USUARIOS.length);
        List<String[]> usuarios = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            usuarios.add(new String[]{String.format("U%03d", i + 1), NOMBRES_USUARIOS[i]});
        }
        return usuarios;
    }

    private void escribirArchivo(String nombre, List<String[]> filas) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(carpetaEntrada + "/" + nombre), StandardCharsets.UTF_8))) {
            for (String[] fila : filas) {
                bw.write(String.join(";", fila));
                bw.newLine();
            }
        }
    }

    private void generarCalificaciones(List<String[]> usuarios, List<String[]> cafes) throws IOException {
        List<String> idsCafes = new ArrayList<>();
        for (String[] c : cafes) idsCafes.add(c[0]);

        for (String[] usuario : usuarios) {
            String idUsuario = usuario[0];
            int numCalifs = MIN_CALIF_POR_USUARIO
                    + random.nextInt(MAX_CALIF_POR_USUARIO - MIN_CALIF_POR_USUARIO + 1);

            List<String> cafesShuffle = new ArrayList<>(idsCafes);
            Collections.shuffle(cafesShuffle, random);

            Set<String> calificados = new HashSet<>();
            List<String> lineas = new ArrayList<>();

            for (String idCafe : cafesShuffle) {
                if (lineas.size() >= numCalifs) break;
                lineas.add(idCafe + ";" + (1 + random.nextInt(5)));
                calificados.add(idCafe);
            }

            int numCorruptos = Math.max(1,
                    (int) Math.round(lineas.size() * PORCENTAJE_CORRUPTOS / (1 - PORCENTAJE_CORRUPTOS)));
            for (int i = 0; i < numCorruptos; i++) {
                int pos = random.nextInt(lineas.size() + 1);
                lineas.add(pos, generarLineaCorrupta(idsCafes, calificados));
            }

            String archivo = carpetaEntrada + "/calificaciones_" + idUsuario + ".txt";
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
                for (String linea : lineas) {
                    bw.write(linea);
                    bw.newLine();
                }
            }
        }
    }

    private String generarLineaCorrupta(List<String> idsCafes, Set<String> calificados) {
        return switch (random.nextInt(4)) {
            case 0 -> "XINVALID;" + (1 + random.nextInt(5));
            case 1 -> idsCafes.get(random.nextInt(idsCafes.size())) + ";" + (6 + random.nextInt(5));
            case 2 -> idsCafes.get(random.nextInt(idsCafes.size()));
            case 3 -> {
                if (!calificados.isEmpty()) {
                    yield calificados.iterator().next() + ";" + (1 + random.nextInt(5));
                }
                yield "XINVALID;0";
            }
            default -> ";;";
        };
    }
}

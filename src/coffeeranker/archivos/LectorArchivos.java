package coffeeranker.archivos;

import coffeeranker.modelo.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LectorArchivos {
    private final String carpetaEntrada;

    public LectorArchivos(String carpetaEntrada) {
        this.carpetaEntrada = carpetaEntrada;
    }

    public Map<String, Finca> leerFincas() throws IOException {
        Map<String, Finca> mapa = new HashMap<>();
        try (BufferedReader br = abrirLector("fincas.csv")) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                if (c.length == 3 && !c[0].isBlank()) {
                    Finca f = new Finca(c[0].trim(), c[1].trim(), c[2].trim());
                    mapa.put(f.getId(), f);
                }
            }
        }
        return mapa;
    }

    public Map<String, Cafe> leerCafes() throws IOException {
        Map<String, Cafe> mapa = new HashMap<>();
        try (BufferedReader br = abrirLector("cafes.csv")) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                if (c.length == 4 && !c[0].isBlank()) {
                    Cafe cafe = new Cafe(c[0].trim(), c[1].trim(), c[2].trim(), c[3].trim());
                    mapa.put(cafe.getId(), cafe);
                }
            }
        }
        return mapa;
    }

    public Map<String, Usuario> leerUsuarios() throws IOException {
        Map<String, Usuario> mapa = new HashMap<>();
        try (BufferedReader br = abrirLector("usuarios.csv")) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                if (c.length == 2 && !c[0].isBlank()) {
                    Usuario u = new Usuario(c[0].trim(), c[1].trim());
                    mapa.put(u.getId(), u);
                }
            }
        }
        return mapa;
    }

    public void leerCalificaciones(
            Map<String, Cafe> catalogoCafes,
            Map<String, Usuario> catalogoUsuarios,
            Map<String, List<Integer>> acumulador,
            List<RegistroDescartado> descartados) throws IOException {

        File[] archivos = Paths.get(carpetaEntrada).toFile()
                .listFiles((d, n) -> n.startsWith("calificaciones_") && n.endsWith(".txt"));

        if (archivos == null) return;

        for (File archivo : archivos) {
            String nombreArchivo = archivo.getName();
            String idUsuario = nombreArchivo
                    .replace("calificaciones_", "")
                    .replace(".txt", "");

            if (!catalogoUsuarios.containsKey(idUsuario)) {
                descartados.add(new RegistroDescartado(
                        nombreArchivo, 0, "", "Usuario no registrado en catálogo"));
                continue;
            }

            Set<String> cafesCalificados = new HashSet<>();
            int numLinea = 0;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(archivo), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    numLinea++;
                    String[] c = linea.split(";", -1);

                    if (c.length != 2 || c[0].isBlank() || c[1].isBlank()) {
                        descartados.add(new RegistroDescartado(
                                nombreArchivo, numLinea, linea, "Formato inválido"));
                        continue;
                    }

                    String idCafe = c[0].trim();
                    String estrellasStr = c[1].trim();

                    if (!catalogoCafes.containsKey(idCafe)) {
                        descartados.add(new RegistroDescartado(
                                nombreArchivo, numLinea, linea, "Café no existe en catálogo"));
                        continue;
                    }

                    int estrellas;
                    try {
                        estrellas = Integer.parseInt(estrellasStr);
                        if (estrellas < 1 || estrellas > 5) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        descartados.add(new RegistroDescartado(
                                nombreArchivo, numLinea, linea, "Estrellas fuera de rango"));
                        continue;
                    }

                    if (cafesCalificados.contains(idCafe)) {
                        descartados.add(new RegistroDescartado(
                                nombreArchivo, numLinea, linea, "Calificación duplicada"));
                        continue;
                    }

                    cafesCalificados.add(idCafe);
                    acumulador.computeIfAbsent(idCafe, k -> new ArrayList<>()).add(estrellas);
                }
            }
        }
    }

    private BufferedReader abrirLector(String nombreArchivo) throws IOException {
        return new BufferedReader(new InputStreamReader(
                new FileInputStream(carpetaEntrada + "/" + nombreArchivo), StandardCharsets.UTF_8));
    }
}

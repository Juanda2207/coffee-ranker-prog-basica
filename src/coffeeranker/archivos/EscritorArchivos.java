package coffeeranker.archivos;

import coffeeranker.modelo.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class EscritorArchivos {
    private final String carpetaSalida;

    public EscritorArchivos(String carpetaSalida) {
        this.carpetaSalida = carpetaSalida;
    }

    public void escribirRankingGeneral(List<ResultadoCafe> resultados) throws IOException {
        Files.createDirectories(Paths.get(carpetaSalida));
        try (BufferedWriter bw = abrirEscritor("ranking_general.csv")) {
            bw.write("POSICION;ID_CAFE;NOMBRE;FINCA;CATEGORIA;PROMEDIO;NUM_CALIFICACIONES");
            bw.newLine();
            for (ResultadoCafe r : resultados) {
                bw.write(String.format(Locale.US, "%d;%s;%s;%s;%s;%.2f;%d",
                        r.getPosicion(),
                        r.getCafe().getId(),
                        r.getCafe().getNombre(),
                        r.getFinca() != null ? r.getFinca().getNombre() : "",
                        r.getCafe().getCategoria(),
                        r.getPromedio(),
                        r.getNumCalificaciones()));
                bw.newLine();
            }
        }
    }

    public void escribirRankingPorCategoria(Map<String, List<ResultadoCafe>> porCategoria) throws IOException {
        Files.createDirectories(Paths.get(carpetaSalida));
        try (BufferedWriter bw = abrirEscritor("ranking_por_categoria.csv")) {
            bw.write("CATEGORIA;POSICION;ID_CAFE;NOMBRE;FINCA;PROMEDIO;NUM_CALIFICACIONES");
            bw.newLine();
            for (Map.Entry<String, List<ResultadoCafe>> entry : porCategoria.entrySet()) {
                int pos = 1;
                for (ResultadoCafe r : entry.getValue()) {
                    bw.write(String.format(Locale.US, "%s;%d;%s;%s;%s;%.2f;%d",
                            entry.getKey(),
                            pos++,
                            r.getCafe().getId(),
                            r.getCafe().getNombre(),
                            r.getFinca() != null ? r.getFinca().getNombre() : "",
                            r.getPromedio(),
                            r.getNumCalificaciones()));
                    bw.newLine();
                }
            }
        }
    }

    public void escribirRankingPorFinca(Map<String, List<ResultadoCafe>> porFinca) throws IOException {
        Files.createDirectories(Paths.get(carpetaSalida));
        try (BufferedWriter bw = abrirEscritor("ranking_por_finca.csv")) {
            bw.write("FINCA;POSICION;ID_CAFE;NOMBRE;CATEGORIA;PROMEDIO;NUM_CALIFICACIONES");
            bw.newLine();
            for (Map.Entry<String, List<ResultadoCafe>> entry : porFinca.entrySet()) {
                int pos = 1;
                for (ResultadoCafe r : entry.getValue()) {
                    bw.write(String.format(Locale.US, "%s;%d;%s;%s;%s;%.2f;%d",
                            entry.getKey(),
                            pos++,
                            r.getCafe().getId(),
                            r.getCafe().getNombre(),
                            r.getCafe().getCategoria(),
                            r.getPromedio(),
                            r.getNumCalificaciones()));
                    bw.newLine();
                }
            }
        }
    }

    public void escribirDescartados(List<RegistroDescartado> descartados) throws IOException {
        Files.createDirectories(Paths.get(carpetaSalida));
        try (BufferedWriter bw = abrirEscritor("registros_descartados.txt")) {
            bw.write("ARCHIVO;LINEA;CONTENIDO;MOTIVO");
            bw.newLine();
            for (RegistroDescartado rd : descartados) {
                bw.write(rd.getArchivo() + ";" + rd.getNumeroLinea()
                        + ";" + rd.getContenido() + ";" + rd.getMotivo());
                bw.newLine();
            }
        }
    }

    public void escribirResumenFincas(
            Map<String, Finca> catalogoFincas,
            Map<String, List<ResultadoCafe>> porFinca) throws IOException {
        Files.createDirectories(Paths.get(carpetaSalida));
        try (BufferedWriter bw = abrirEscritor("resumen_fincas.csv")) {
            bw.write("ID_FINCA;NOMBRE;DEPARTAMENTO;NUM_CAFES_CLASIFICADOS;PROMEDIO_FINCA");
            bw.newLine();
            for (Map.Entry<String, List<ResultadoCafe>> entry : porFinca.entrySet()) {
                String nombreFinca = entry.getKey();
                List<ResultadoCafe> cafes = entry.getValue();

                Finca finca = catalogoFincas.values().stream()
                        .filter(f -> f.getNombre().equals(nombreFinca))
                        .findFirst().orElse(null);
                String idFinca = finca != null ? finca.getId() : "";
                String departamento = finca != null ? finca.getDepartamento() : "";

                double promedioFinca = cafes.stream()
                        .mapToDouble(ResultadoCafe::getPromedio)
                        .average().orElse(0.0);
                promedioFinca = Math.round(promedioFinca * 100.0) / 100.0;

                bw.write(String.format(Locale.US, "%s;%s;%s;%d;%.2f",
                        idFinca, nombreFinca, departamento, cafes.size(), promedioFinca));
                bw.newLine();
            }
        }
    }

    private BufferedWriter abrirEscritor(String nombre) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(carpetaSalida + "/" + nombre), StandardCharsets.UTF_8));
        bw.write('﻿'); // BOM para que Excel reconozca UTF-8
        return bw;
    }
}

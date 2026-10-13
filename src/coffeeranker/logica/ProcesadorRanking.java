package coffeeranker.logica;

import coffeeranker.modelo.*;
import java.util.*;

public class ProcesadorRanking {
    private static final int MIN_CALIFICACIONES = 3;

    public List<ResultadoCafe> calcularResultados(
            Map<String, Cafe> catalogoCafes,
            Map<String, Finca> catalogoFincas,
            Map<String, List<Integer>> acumulador,
            List<RegistroDescartado> descartados) {

        List<ResultadoCafe> resultados = new ArrayList<>();

        for (Map.Entry<String, List<Integer>> entry : acumulador.entrySet()) {
            String idCafe = entry.getKey();
            List<Integer> estrellas = entry.getValue();

            if (estrellas.size() < MIN_CALIFICACIONES) {
                descartados.add(new RegistroDescartado("acumulador", 0, idCafe,
                        "Calificaciones insuficientes (" + estrellas.size() + "<" + MIN_CALIFICACIONES + ")"));
                continue;
            }

            Cafe cafe = catalogoCafes.get(idCafe);
            if (cafe == null) continue;

            Finca finca = catalogoFincas.get(cafe.getIdFinca());
            int suma = estrellas.stream().mapToInt(Integer::intValue).sum();
            double promedio = Math.round((double) suma / estrellas.size() * 100.0) / 100.0;

            resultados.add(new ResultadoCafe(cafe, finca, promedio, estrellas.size()));
        }

        resultados.sort(
            Comparator.comparingDouble(ResultadoCafe::getPromedio).reversed()
                .thenComparing(ResultadoCafe::getNumCalificaciones, Comparator.reverseOrder())
                .thenComparing(r -> r.getCafe().getNombre()));

        for (int i = 0; i < resultados.size(); i++) {
            resultados.get(i).setPosicion(i + 1);
        }

        return resultados;
    }

    public Map<String, List<ResultadoCafe>> agruparPorCategoria(List<ResultadoCafe> resultados) {
        Map<String, List<ResultadoCafe>> mapa = new LinkedHashMap<>();
        for (ResultadoCafe r : resultados) {
            mapa.computeIfAbsent(r.getCafe().getCategoria(), k -> new ArrayList<>()).add(r);
        }
        return mapa;
    }

    public Map<String, List<ResultadoCafe>> agruparPorFinca(List<ResultadoCafe> resultados) {
        Map<String, List<ResultadoCafe>> mapa = new LinkedHashMap<>();
        for (ResultadoCafe r : resultados) {
            String nombreFinca = r.getFinca() != null ? r.getFinca().getNombre() : "Desconocida";
            mapa.computeIfAbsent(nombreFinca, k -> new ArrayList<>()).add(r);
        }
        return mapa;
    }
}

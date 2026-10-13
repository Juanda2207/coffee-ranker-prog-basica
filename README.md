# Coffee Ranker

Aplicación de escritorio en Java que procesa calificaciones de cafés colombianos y genera un ranking por promedio de estrellas.

**Materia:** Conceptos Fundamentales de Programación  
**Institución:** Politécnico Grancolombiano  
**Autor:** Juan David Bonilla

---

## Requisitos

- Java 21 o superior
- Eclipse IDE (opcional, el proyecto incluye `.project` y `.classpath`)

## Estructura del proyecto

```
CoffeeRanker/
├── src/
│   └── coffeeranker/
│       ├── app/          # Punto de entrada (Main.java)
│       ├── modelo/       # Clases de datos (Cafe, Finca, Usuario, …)
│       ├── archivos/     # Lectura y escritura de CSV/TXT
│       ├── generacion/   # Generador de datos de prueba
│       ├── logica/       # Cálculo y ordenación del ranking
│       └── ui/           # Interfaz gráfica Swing
├── data/
│   ├── entrada/          # fincas.csv, cafes.csv, usuarios.csv, calificaciones_Uxx.txt
│   └── salida/           # Archivos generados por la aplicación
├── bin/                  # Clases compiladas
└── conclusiones.txt
```

## Archivos de entrada

| Archivo | Descripción |
|---|---|
| `fincas.csv` | Catálogo de fincas (`ID_FINCA;NOMBRE;DEPARTAMENTO`) |
| `cafes.csv` | Catálogo de cafés (`ID_CAFE;NOMBRE;CATEGORIA;ID_FINCA`) |
| `usuarios.csv` | Lista de catadores (`ID_USUARIO;NOMBRE;REGION`) |
| `calificaciones_Uxx.txt` | Calificaciones por usuario (`ID_CAFE;ESTRELLAS`) |

La carpeta de entrada es configurable desde la pantalla **Base de Datos** de la aplicación.

## Archivos de salida

| Archivo | Descripción |
|---|---|
| `ranking_general.csv` | Ranking completo ordenado por promedio |
| `ranking_por_categoria.csv` | Ranking separado por categoría de café |
| `ranking_por_finca.csv` | Ranking agrupado por finca |
| `resumen_fincas.csv` | Promedio y cantidad de cafés clasificados por finca |
| `registros_descartados.txt` | Líneas inválidas con motivo de descarte |

Todos los CSV se generan con BOM UTF-8 para compatibilidad con Excel.

## Cómo ejecutar

### Desde Eclipse

1. Importar el proyecto: **File → Import → Existing Projects into Workspace**.
2. Copiar `src/coffeeranker/ui/logo.png` a `bin/coffeeranker/ui/logo.png` si el ícono no aparece.
3. Ejecutar `coffeeranker.app.Main`.

### Desde la línea de comandos

```bash
cd CoffeeRanker
javac -d bin -sourcepath src src/coffeeranker/app/Main.java
java -cp bin coffeeranker.app.Main
```

## Flujo de uso

1. **Base de Datos** — elegir carpeta de entrada y generar o importar los archivos base.
2. **Procesar Ranking** — calcular promedios; requiere que la base esté cargada.
3. **Descargar Resultados** — guardar los cinco archivos CSV/TXT de salida.

Un café necesita al menos **3 calificaciones válidas** para aparecer en el ranking.

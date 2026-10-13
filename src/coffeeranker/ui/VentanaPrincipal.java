package coffeeranker.ui;

import coffeeranker.archivos.*;
import coffeeranker.generacion.*;
import coffeeranker.logica.*;
import coffeeranker.modelo.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private static final String CARPETA_SALIDA_DEFAULT = "data/salida";
    private static final Color COLOR_SIDEBAR  = new Color(40, 40, 40);
    private static final Color COLOR_BTN_IDLE = new Color(65, 65, 65);
    private static final Color COLOR_BTN_OVER = new Color(95, 95, 95);
    private static final Color COLOR_FILA_PAR = new Color(245, 248, 252);

    // estado de la aplicación
    private String carpetaEntrada = "data/entrada";
    private Map<String, Finca>   fincas;
    private Map<String, Cafe>    cafes;
    private Map<String, Usuario> usuarios;
    private List<ResultadoCafe>       resultados;
    private List<RegistroDescartado>  descartados;

    private JPanel    panelContenido;
    private CardLayout cardLayout;
    private JLabel    lblEstado;

    // botones que dependen del estado
    private JButton btnProcesar;
    private JButton btnGuardar;

    public VentanaPrincipal() {
        super("Coffee Ranker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        cargarIcono();
        construirUI();
        actualizarEstadoBotones();
    }

    private void cargarIcono() {
        try (var is = getClass().getResourceAsStream("logo.png")) {
            if (is != null) setIconImage(new ImageIcon(is.readAllBytes()).getImage());
        } catch (Exception ignored) {}
    }

    // ─── Construcción general ──────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(crearMenuLateral(), BorderLayout.WEST);

        cardLayout     = new CardLayout();
        panelContenido = new JPanel(cardLayout);
        panelContenido.add(crearPanelBienvenida(), "bienvenida");
        panelContenido.add(crearPanelGenerar(),    "generar");
        panelContenido.add(crearPanelBase(),        "base");
        panelContenido.add(crearPanelRanking(),     "ranking");
        panelContenido.add(crearPanelDescarga(),    "descarga");
        add(panelContenido, BorderLayout.CENTER);

        lblEstado = new JLabel(" Listo.");
        lblEstado.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        add(lblEstado, BorderLayout.SOUTH);
    }

    private JPanel crearMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(COLOR_SIDEBAR);
        menu.setPreferredSize(new Dimension(210, 0));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));

        try (var is = getClass().getResourceAsStream("logo.png")) {
            if (is != null) {
                ImageIcon icon = new ImageIcon(
                        new ImageIcon(is.readAllBytes()).getImage()
                                .getScaledInstance(64, 64, Image.SCALE_SMOOTH));
                JLabel imgLogo = new JLabel(icon);
                imgLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
                menu.add(imgLogo);
                menu.add(Box.createVerticalStrut(8));
            }
        } catch (Exception ignored) {}

        JLabel logo = new JLabel("Coffee Ranker");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 16));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(logo);
        menu.add(Box.createVerticalStrut(24));

        String[][] opciones = {
            {"1. Generar archivos",  "generar"},
            {"2. Importar base",      "base"},
            {"3. Ver ranking",        "ranking"},
            {"4. Descargar salida",   "descarga"},
            {"5. Salir",              "salir"}
        };

        for (String[] op : opciones) {
            JButton btn = crearBotonSidebar(op[0]);
            final String destino = op[1];
            btn.addActionListener(e -> manejarOpcion(destino));
            menu.add(btn);
            menu.add(Box.createVerticalStrut(6));
        }
        return menu;
    }

    private JButton crearBotonSidebar(String texto) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 40));
        btn.setBackground(COLOR_BTN_IDLE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_BTN_OVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(COLOR_BTN_IDLE); }
            @Override public void mousePressed(MouseEvent e) { btn.setBackground(COLOR_BTN_IDLE.darker()); }
            @Override public void mouseReleased(MouseEvent e){ btn.setBackground(COLOR_BTN_OVER); }
        });
        return btn;
    }

    private void manejarOpcion(String destino) {
        if ("salir".equals(destino)) {
            int c = JOptionPane.showConfirmDialog(this,
                    "¿Deseas salir de Coffee Ranker?", "Salir", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) System.exit(0);
        } else {
            cardLayout.show(panelContenido, destino);
        }
    }

    private void actualizarEstadoBotones() {
        boolean baseLista    = fincas != null && cafes != null && usuarios != null;
        boolean rankingListo = resultados != null;
        if (btnProcesar != null) btnProcesar.setEnabled(baseLista);
        if (btnGuardar  != null) btnGuardar.setEnabled(rankingListo);
    }

    // ─── Panel bienvenida ──────────────────────────────────────────────────────

    private JPanel crearPanelBienvenida() {
        JPanel p = new JPanel(new GridBagLayout());
        JLabel lbl = new JLabel("<html><center>"
                + "<h2 style='font-size:20px'>☕ Coffee Ranker</h2>"
                + "<p>Sistema de ranking de cafés de especialidad</p>"
                + "<br><p>Selecciona una opción del menú lateral para comenzar.</p>"
                + "</center></html>");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lbl);
        return p;
    }

    // ─── Panel generar ─────────────────────────────────────────────────────────

    private JPanel crearPanelGenerar() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Generar archivos de entrada");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        p.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
        campos.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 200));

        JTextField txtFincas   = new JTextField("8");
        JTextField txtCafes    = new JTextField("25");
        JTextField txtUsuarios = new JTextField("40");
        JTextField txtSemilla  = new JTextField("");

        campos.add(new JLabel("Número de fincas (máx 8):")); campos.add(txtFincas);
        campos.add(new JLabel("Número de cafés:"));           campos.add(txtCafes);
        campos.add(new JLabel("Número de usuarios (máx 40):"));campos.add(txtUsuarios);
        campos.add(new JLabel("Semilla (vacío = aleatorio):")); campos.add(txtSemilla);

        JTextArea log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JButton btnGenerar = new JButton("Generar archivos");
        btnGenerar.setBackground(new Color(34, 139, 34));
        btnGenerar.setForeground(Color.WHITE);
        btnGenerar.setOpaque(true);
        btnGenerar.setBorderPainted(false);
        btnGenerar.addActionListener(e -> {
            try {
                int nF = Integer.parseInt(txtFincas.getText().trim());
                int nC = Integer.parseInt(txtCafes.getText().trim());
                int nU = Integer.parseInt(txtUsuarios.getText().trim());
                Long semilla = txtSemilla.getText().isBlank()
                        ? null : Long.parseLong(txtSemilla.getText().trim());

                new GeneradorDatos(carpetaEntrada, semilla).generar(nF, nC, nU);

                // resetear estado derivado al regenerar
                fincas   = null;
                cafes    = null;
                usuarios = null;
                resultados  = null;
                descartados = null;
                actualizarEstadoBotones();

                log.setText("Archivos generados en: " + carpetaEntrada + "\n\n");
                log.append("  fincas.csv       → " + nF + " fincas\n");
                log.append("  cafes.csv        → " + nC + " cafés\n");
                log.append("  usuarios.csv     → " + nU + " usuarios\n");
                log.append("  calificaciones_Uxxx.txt → uno por usuario\n\n");
                log.append("Semilla: " + (semilla != null ? semilla : "aleatoria"));
                lblEstado.setText(" Archivos generados en " + carpetaEntrada);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(p, "Ingresa valores numéricos válidos.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(p, "Error al generar: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel norte = new JPanel(new BorderLayout(8, 8));
        norte.add(campos, BorderLayout.CENTER);
        norte.add(btnGenerar, BorderLayout.SOUTH);

        p.add(norte, BorderLayout.NORTH);
        p.add(new JScrollPane(log), BorderLayout.CENTER);
        return p;
    }

    // ─── Panel base ────────────────────────────────────────────────────────────

    private JPanel crearPanelBase() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // encabezado con carpeta configurable
        JPanel norte = new JPanel(new BorderLayout(8, 8));
        JLabel titulo = new JLabel("Importar y ver base de datos");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        norte.add(titulo, BorderLayout.NORTH);

        JPanel filaCarpeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filaCarpeta.add(new JLabel("Carpeta de entrada:"));
        JTextField txtCarpetaEntrada = new JTextField(carpetaEntrada, 28);
        JButton btnElegirEntrada = new JButton("Elegir...");
        btnElegirEntrada.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(carpetaEntrada);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(p) == JFileChooser.APPROVE_OPTION) {
                carpetaEntrada = fc.getSelectedFile().getAbsolutePath();
                txtCarpetaEntrada.setText(carpetaEntrada);
            }
        });
        txtCarpetaEntrada.addActionListener(e -> carpetaEntrada = txtCarpetaEntrada.getText().trim());
        filaCarpeta.add(txtCarpetaEntrada);
        filaCarpeta.add(btnElegirEntrada);
        norte.add(filaCarpeta, BorderLayout.CENTER);
        p.add(norte, BorderLayout.NORTH);

        // tablas
        DefaultTableModel mFincas   = modelo("ID_FINCA", "NOMBRE", "DEPARTAMENTO");
        DefaultTableModel mCafes    = modelo("ID_CAFE", "NOMBRE", "CATEGORIA", "ID_FINCA");
        DefaultTableModel mUsuarios = modelo("ID_USUARIO", "NOMBRE");
        JTextArea areaCalif = areaMonoespaciada();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Fincas",         new JScrollPane(tablaConColores(mFincas)));
        tabs.addTab("Cafés",          new JScrollPane(tablaConColores(mCafes)));
        tabs.addTab("Usuarios",       new JScrollPane(tablaConColores(mUsuarios)));
        tabs.addTab("Calificaciones", new JScrollPane(areaCalif));
        p.add(tabs, BorderLayout.CENTER);

        JButton btnImportar = new JButton("Importar base");
        btnImportar.addActionListener(e -> {
            carpetaEntrada = txtCarpetaEntrada.getText().trim();
            try {
                LectorArchivos lector = new LectorArchivos(carpetaEntrada);
                fincas   = lector.leerFincas();
                cafes    = lector.leerCafes();
                usuarios = lector.leerUsuarios();

                // resetear ranking al reimportar
                resultados  = null;
                descartados = null;
                actualizarEstadoBotones();

                mFincas.setRowCount(0);
                fincas.values().forEach(f -> mFincas.addRow(
                        new Object[]{f.getId(), f.getNombre(), f.getDepartamento()}));

                mCafes.setRowCount(0);
                cafes.values().forEach(c -> mCafes.addRow(
                        new Object[]{c.getId(), c.getNombre(), c.getCategoria(), c.getIdFinca()}));

                mUsuarios.setRowCount(0);
                usuarios.values().forEach(u -> mUsuarios.addRow(
                        new Object[]{u.getId(), u.getNombre()}));

                areaCalif.setText("Archivos de calificación en " + carpetaEntrada + ":\n");
                File[] archivosCalif = new File(carpetaEntrada)
                        .listFiles((d, n) -> n.startsWith("calificaciones_") && n.endsWith(".txt"));
                if (archivosCalif != null) {
                    for (File af : archivosCalif) areaCalif.append("  " + af.getName() + "\n");
                    areaCalif.append("\nTotal: " + archivosCalif.length + " archivo(s)");
                }

                lblEstado.setText(" Base importada: " + fincas.size() + " fincas · "
                        + cafes.size() + " cafés · " + usuarios.size() + " usuarios");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(p,
                        "Error al importar: " + ex.getMessage()
                        + "\n¿Generaste los archivos primero (opción 1)?",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(btnImportar, BorderLayout.SOUTH);
        return p;
    }

    // ─── Panel ranking ─────────────────────────────────────────────────────────

    private JPanel crearPanelRanking() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Ranking de cafés de especialidad");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        p.add(titulo, BorderLayout.NORTH);

        DefaultTableModel mGeneral   = modelo("Pos","ID","Nombre","Finca","Categoría","Promedio","Calificaciones");
        DefaultTableModel mCategoria = modelo("Categoría","Pos","ID","Nombre","Finca","Promedio","Calificaciones");
        DefaultTableModel mFinca     = modelo("Finca","Pos","ID","Nombre","Categoría","Promedio","Calificaciones");
        DefaultTableModel mDesc      = modelo("Archivo","Línea","Contenido","Motivo");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("General",       new JScrollPane(tablaConColores(mGeneral)));
        tabs.addTab("Por categoría", new JScrollPane(tablaConColores(mCategoria)));
        tabs.addTab("Por finca",     new JScrollPane(tablaConColores(mFinca)));
        tabs.addTab("Descartados",   new JScrollPane(tablaConColores(mDesc)));
        p.add(tabs, BorderLayout.CENTER);

        btnProcesar = new JButton("Procesar ranking");
        btnProcesar.setBackground(new Color(0, 102, 204));
        btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setOpaque(true);
        btnProcesar.setBorderPainted(false);
        btnProcesar.addActionListener(e -> {
            try {
                LectorArchivos lector = new LectorArchivos(carpetaEntrada);
                Map<String, List<Integer>> acumulador = new HashMap<>();
                descartados = new ArrayList<>();
                lector.leerCalificaciones(cafes, usuarios, acumulador, descartados);

                ProcesadorRanking proc = new ProcesadorRanking();
                resultados = proc.calcularResultados(cafes, fincas, acumulador, descartados);
                actualizarEstadoBotones();

                mGeneral.setRowCount(0);
                for (ResultadoCafe r : resultados) {
                    mGeneral.addRow(new Object[]{
                        r.getPosicion(), r.getCafe().getId(), r.getCafe().getNombre(),
                        r.getFinca() != null ? r.getFinca().getNombre() : "",
                        r.getCafe().getCategoria(),
                        String.format(Locale.US, "%.2f", r.getPromedio()),
                        r.getNumCalificaciones()
                    });
                }

                mCategoria.setRowCount(0);
                for (Map.Entry<String, List<ResultadoCafe>> en : proc.agruparPorCategoria(resultados).entrySet()) {
                    int pos = 1;
                    for (ResultadoCafe r : en.getValue()) {
                        mCategoria.addRow(new Object[]{
                            en.getKey(), pos++, r.getCafe().getId(), r.getCafe().getNombre(),
                            r.getFinca() != null ? r.getFinca().getNombre() : "",
                            String.format(Locale.US, "%.2f", r.getPromedio()),
                            r.getNumCalificaciones()
                        });
                    }
                }

                mFinca.setRowCount(0);
                for (Map.Entry<String, List<ResultadoCafe>> en : proc.agruparPorFinca(resultados).entrySet()) {
                    int pos = 1;
                    for (ResultadoCafe r : en.getValue()) {
                        mFinca.addRow(new Object[]{
                            en.getKey(), pos++, r.getCafe().getId(), r.getCafe().getNombre(),
                            r.getCafe().getCategoria(),
                            String.format(Locale.US, "%.2f", r.getPromedio()),
                            r.getNumCalificaciones()
                        });
                    }
                }

                mDesc.setRowCount(0);
                for (RegistroDescartado rd : descartados) {
                    mDesc.addRow(new Object[]{
                        rd.getArchivo(), rd.getNumeroLinea(), rd.getContenido(), rd.getMotivo()
                    });
                }

                lblEstado.setText(" Ranking procesado: " + resultados.size()
                        + " cafés clasificados · " + descartados.size() + " registros descartados");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(p,
                        "Error al leer calificaciones: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p.add(btnProcesar, BorderLayout.SOUTH);
        return p;
    }

    // ─── Panel descarga ────────────────────────────────────────────────────────

    private JPanel crearPanelDescarga() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Descargar archivos de salida");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        p.add(titulo, BorderLayout.NORTH);

        JTextField txtCarpeta = new JTextField(CARPETA_SALIDA_DEFAULT);
        JButton btnElegir = new JButton("Elegir carpeta...");
        btnElegir.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(p) == JFileChooser.APPROVE_OPTION) {
                txtCarpeta.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JTextArea log = areaMonoespaciada();
        btnGuardar = new JButton("Guardar archivos de salida");
        btnGuardar.setBackground(new Color(34, 139, 34));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorderPainted(false);
        btnGuardar.addActionListener(e -> {
            try {
                String carpeta = txtCarpeta.getText().trim();
                EscritorArchivos escritor = new EscritorArchivos(carpeta);
                ProcesadorRanking proc    = new ProcesadorRanking();
                Map<String, List<ResultadoCafe>> porFinca = proc.agruparPorFinca(resultados);

                escritor.escribirRankingGeneral(resultados);
                escritor.escribirRankingPorCategoria(proc.agruparPorCategoria(resultados));
                escritor.escribirRankingPorFinca(porFinca);
                escritor.escribirDescartados(descartados != null ? descartados : new ArrayList<>());
                escritor.escribirResumenFincas(fincas, porFinca);

                log.setText("Archivos guardados en: " + carpeta + "\n\n");
                log.append("  ranking_general.csv\n");
                log.append("  ranking_por_categoria.csv\n");
                log.append("  ranking_por_finca.csv\n");
                log.append("  registros_descartados.txt\n");
                log.append("  resumen_fincas.csv\n");
                lblEstado.setText(" Archivos de salida guardados en " + carpeta);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(p,
                        "Error al guardar: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        opciones.add(new JLabel("Carpeta de salida:"));
        opciones.add(txtCarpeta);
        opciones.add(btnElegir);
        opciones.add(btnGuardar);

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.add(opciones, BorderLayout.NORTH);
        centro.add(new JScrollPane(log), BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    // ─── Utilidades ────────────────────────────────────────────────────────────

    private static JTextArea areaMonoespaciada() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setFont(new Font("Monospaced", Font.PLAIN, 11));
        return a;
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private static JTable tablaConColores(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : COLOR_FILA_PAR);
                }
                return c;
            }
        };
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }
}

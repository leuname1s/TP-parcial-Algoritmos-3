package GUI;

import Controladores.ControladorJuego;
import Controladores.EstadoVistaJuego;
import Controladores.EstadoVistaJuego.Guardado;
import Interfaces.IVistaJuego;
import datos.EstadoPartida;
import datos.Estadisticas;
import datos.ModoJuego;
import datos.Participante;
import datos.Personaje;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Una sola ventana; la vista presenta instantáneas y delega todas las acciones al controlador. */
@SuppressWarnings("serial") // La ventana no se usa como formato de persistencia.
public final class VentanaPrincipal extends JFrame implements IVistaJuego {
    private static final long serialVersionUID = 1L;
    static { Tema.instalar(); }
    private ControladorJuego controlador;
    private final CardLayout pantallas = new CardLayout();
    private final JPanel contenido = Tema.panel(pantallas);
    private final JLabel situacion = new JLabel("Elegí cómo jugar", SwingConstants.RIGHT);
    private final JLabel pie = new JLabel(" ");
    private final JButton menu = Tema.boton("Menú", () -> controlador.volverAlMenu());
    private final JTextField usuario = new JTextField(20);
    private final JLabel estadisticasUsuario = new JLabel();
    private final JLabel estadisticasMaquinas = new JLabel();
    private final PanelTablero seleccion = new PanelTablero("Elegí tu secreto", id -> actualizarSeleccion());
    private final PanelTablero tablero = new PanelTablero("Tu tablero", id -> actualizarRiesgo());
    private final PanelTablero tableroM1 = new PanelTablero("Máquina 1", id -> { });
    private final PanelTablero tableroM2 = new PanelTablero("Máquina 2", id -> { });
    private final JButton elegir = Tema.boton("Elegir personaje", () -> {
        if (seleccion.seleccion() != null) controlador.seleccionarPersonaje(seleccion.seleccion());
    });
    private final JLabel elegido = new JLabel("Seleccioná una carta para que sea tu secreto.");
    private final JButton arriesgar = Tema.boton("Arriesgar personaje", () -> {
        if (tablero.seleccion() != null) controlador.arriesgar(tablero.seleccion());
    });
    private final JLabel candidato = new JLabel("Seleccioná una carta para arriesgar.");
    private final PanelPreguntas preguntas = new PanelPreguntas(p -> controlador.preguntar(p));
    private final PanelSecreto secretoJugador = new PanelSecreto("Tu personaje secreto");
    private final PanelSecreto secretoM1 = new PanelSecreto("Secreto de la Máquina 1");
    private final PanelSecreto secretoM2 = new PanelSecreto("Secreto de la Máquina 2");
    private final PanelMiniTablero miniTablero = new PanelMiniTablero();
    private final PanelHistorial historialHumano = new PanelHistorial(false);
    private final PanelHistorial historialEspectador = new PanelHistorial(true);
    private final PanelHistorial historialResultado = new PanelHistorial(true);
    private final JButton pausa = Tema.boton("Pausar", () -> controlador.alternarPausa());
    private final JButton paso = Tema.boton("Siguiente turno", () -> controlador.siguienteTurno());
    private final JComboBox<String> velocidad = new JComboBox<>(new String[]{"Lenta · 1,8 s", "Normal · 0,9 s", "Rápida · 0,45 s"});
    private final JLabel explicacionFase = new JLabel();
    private final JLabel resultadoTitulo = Tema.titulo("");
    private final JLabel resultadoDetalle = new JLabel();
    private final PanelSecreto secretoFinal = new PanelSecreto("Secreto final de tu rival");
    private final PanelSecreto secretoFinalM1 = new PanelSecreto("Secreto de la Máquina 1");
    private final PanelSecreto secretoFinalM2 = new PanelSecreto("Secreto de la Máquina 2");
    private final JLabel guardado = new JLabel();
    private final JButton reintentar = Tema.boton("Reintentar guardado", () -> controlador.reintentarGuardado());
    private final JButton nueva = Tema.boton("Nueva partida", () -> controlador.volverAlMenu());
    private final JButton terminar = Tema.boton("Salir", () -> controlador.solicitarSalir());
    private EstadoVistaJuego estado;
    private String pantallaActual = "";
    private boolean actualizando;
    private final java.util.function.Consumer<String> alCerrar;

    public VentanaPrincipal() {
        this(nombre -> { });
    }

    public VentanaPrincipal(java.util.function.Consumer<String> alCerrar) {
        super("Adivina Quién");
        this.alCerrar = java.util.Objects.requireNonNull(alCerrar);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 660));
        setSize(1366, 768);
        setLocationRelativeTo(null);
        JPanel raiz = Tema.panel(new BorderLayout());
        JPanel cabecera = Tema.panel(new BorderLayout(20, 0));
        cabecera.setBackground(Tema.CELESTE);
        Tema.margen(cabecera, 15);
        JLabel marca = Tema.titulo("Adivina Quién");
        marca.setFont(Tema.FUENTE.deriveFont(Font.BOLD, 24f));
        cabecera.add(marca, BorderLayout.WEST);
        JPanel derecha = new JPanel(new BorderLayout(16, 0));
        derecha.setOpaque(false);
        derecha.add(situacion, BorderLayout.CENTER);
        derecha.add(menu, BorderLayout.EAST);
        cabecera.add(derecha, BorderLayout.CENTER);
        raiz.add(cabecera, BorderLayout.NORTH);
        Tema.margen(contenido, 16);
        contenido.add(crearMenu(), "menu");
        contenido.add(crearSeleccion(), "seleccion");
        contenido.add(crearHumano(), "humano");
        contenido.add(crearEspectador(), "espectador");
        contenido.add(crearDecision(), "decision");
        contenido.add(crearResultado(), "resultado");
        raiz.add(contenido, BorderLayout.CENTER);
        pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)));
        pie.setFont(Tema.FUENTE.deriveFont(12f));
        raiz.add(pie, BorderLayout.SOUTH);
        setContentPane(raiz);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) {
                if (controlador != null) controlador.solicitarSalir();
            }
        });
    }

    public void conectar(ControladorJuego nuevoControlador) {
        controlador = java.util.Objects.requireNonNull(nuevoControlador);
    }

    private JPanel crearMenu() {
        JPanel centro = Tema.panel(new GridBagLayout());
        JPanel caja = Tema.panel(new BorderLayout(18, 22));
        caja.setPreferredSize(new Dimension(870, 470));
        JPanel bienvenida = vertical();
        bienvenida.add(Tema.titulo("Una pregunta puede cambiar toda la partida"));
        bienvenida.add(Box.createVerticalStrut(9));
        bienvenida.add(new JLabel("Elegí tu rival o seguí el razonamiento de las máquinas como espectador."));
        JPanel nombre = Tema.panel(new FlowLayout(FlowLayout.LEADING, 8, 12));
        JLabel etiqueta = new JLabel("Tu nombre:");
        etiqueta.setLabelFor(usuario);
        usuario.getAccessibleContext().setAccessibleName("Nombre del jugador");
        nombre.add(etiqueta);
        nombre.add(usuario);
        nombre.add(Tema.boton("Consultar estadísticas", () -> controlador.consultarEstadisticas(usuario.getText())));
        usuario.addActionListener(evento -> controlador.consultarEstadisticas(usuario.getText()));
        bienvenida.add(nombre);
        caja.add(bienvenida, BorderLayout.NORTH);
        JPanel modos = Tema.panel(new GridLayout(1, 3, 14, 0));
        modos.add(modo("Máquina 1", "Busca la división más desigual y arriesga más rápido.", ModoJuego.JUGADOR_VS_MAQUINA_1));
        modos.add(modo("Máquina 2", "Busca preguntas equilibradas.", ModoJuego.JUGADOR_VS_MAQUINA_2));
        modos.add(modo("Modo espectador", "Dos tableros y decisiones expuestas.", ModoJuego.MAQUINA_1_VS_MAQUINA_2));
        caja.add(modos, BorderLayout.CENTER);
        JPanel marcadores = Tema.panel(new GridLayout(1, 2, 18, 0));
        marcadores.add(estadisticasUsuario);
        marcadores.add(estadisticasMaquinas);
        caja.add(marcadores, BorderLayout.SOUTH);
        centro.add(caja);
        return centro;
    }

    private JPanel modo(String titulo, String descripcion, ModoJuego modo) {
        JPanel panel = tarjeta(new BorderLayout(0, 12));
        panel.add(Tema.titulo(titulo), BorderLayout.NORTH);
        panel.add(new JLabel("<html>" + descripcion + (modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2
                ? "<br><br>No necesitás ingresar un nombre." : "<br><br>Elegí un secreto y descubrí el del rival.") + "</html>"), BorderLayout.CENTER);
        panel.add(Tema.boton(modo == ModoJuego.MAQUINA_1_VS_MAQUINA_2 ? "Observar partida" : "Jugar contra " + titulo,
                () -> controlador.iniciarPartida(usuario.getText(), modo)), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearSeleccion() {
        JPanel panel = Tema.panel(new BorderLayout(0, 12));
        panel.add(new JLabel("Elegí el personaje que la máquina intentará adivinar. Podés consultar sus atributos con el cursor o el teclado."), BorderLayout.NORTH);
        panel.add(seleccion, BorderLayout.CENTER);
        JPanel acciones = Tema.panel(new BorderLayout(10, 0));
        acciones.add(elegido, BorderLayout.CENTER);
        JPanel botones = Tema.panel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        botones.add(Tema.boton("Elegir al azar", () -> controlador.seleccionarPersonajeAleatorio()));
        botones.add(elegir);
        acciones.add(botones, BorderLayout.EAST);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearHumano() {
        JPanel panel = Tema.panel(new BorderLayout(18, 0));
        JPanel juego = Tema.panel(new BorderLayout(0, 6));
        juego.add(tablero, BorderLayout.CENTER);
        JPanel acciones = Tema.panel(new BorderLayout(0, 4));
        JPanel riesgo = Tema.panel(new BorderLayout(8, 0));
        riesgo.add(candidato, BorderLayout.CENTER);
        arriesgar.setBackground(Tema.ROSA);
        riesgo.add(arriesgar, BorderLayout.EAST);
        acciones.add(riesgo, BorderLayout.NORTH);
        acciones.add(preguntas, BorderLayout.CENTER);
        juego.add(acciones, BorderLayout.SOUTH);
        panel.add(juego, BorderLayout.CENTER);
        JPanel lateral = Tema.panel(new BorderLayout(0, 12));
        lateral.setPreferredSize(new Dimension(325, 400));
        JPanel resumen = Tema.panel(new BorderLayout(0, 10));
        resumen.add(secretoJugador, BorderLayout.NORTH);
        resumen.add(miniTablero, BorderLayout.CENTER);
        lateral.add(resumen, BorderLayout.NORTH);
        lateral.add(historialHumano, BorderLayout.CENTER);
        panel.add(lateral, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearEspectador() {
        JPanel panel = Tema.panel(new BorderLayout(0, 10));
        JPanel controles = Tema.panel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        controles.add(pausa);
        controles.add(paso);
        JLabel etiqueta = new JLabel("Velocidad:");
        etiqueta.setLabelFor(velocidad);
        controles.add(etiqueta);
        controles.add(velocidad);
        velocidad.setSelectedIndex(1);
        velocidad.addActionListener(evento -> {
            if (!actualizando && controlador != null) {
                controlador.cambiarDemora(new int[]{1800, 900, 450}[velocidad.getSelectedIndex()]);
            }
        });
        panel.add(controles, BorderLayout.NORTH);
        JPanel tableros = Tema.panel(new GridLayout(1, 2, 16, 0));
        tableros.add(tableroEspectador(secretoM1, tableroM1));
        tableros.add(tableroEspectador(secretoM2, tableroM2));
        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableros, historialEspectador);
        division.setBackground(Tema.FONDO);
        division.setBorder(BorderFactory.createEmptyBorder());
        division.setResizeWeight(.65);
        division.setDividerLocation(340);
        division.setDividerSize(9);
        panel.add(division, BorderLayout.CENTER);
        return panel;
    }

    private JPanel tableroEspectador(PanelSecreto secreto, PanelTablero panelTablero) {
        JPanel panel = Tema.panel(new BorderLayout(0, 8));
        panel.add(secreto, BorderLayout.NORTH);
        panel.add(panelTablero, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearDecision() {
        JPanel centro = Tema.panel(new GridBagLayout());
        JPanel panel = tarjeta(new BorderLayout(0, 18));
        panel.setPreferredSize(new Dimension(760, 340));
        panel.add(Tema.titulo("¡Acertaste! Podés ir por la victoria verdadera"), BorderLayout.NORTH);
        panel.add(explicacionFase, BorderLayout.CENTER);
        JPanel acciones = Tema.panel(new FlowLayout(FlowLayout.TRAILING, 10, 0));
        acciones.add(Tema.boton("Conservar victoria", () -> controlador.decidirSegundaFase(false)));
        acciones.add(Tema.boton("Aceptar segunda fase", () -> controlador.decidirSegundaFase(true)));
        panel.add(acciones, BorderLayout.SOUTH);
        centro.add(panel);
        return centro;
    }

    private JPanel crearResultado() {
        JPanel panel = Tema.panel(new BorderLayout(0, 14));
        JPanel cabecera = Tema.panel(new BorderLayout(12, 8));
        cabecera.add(resultadoTitulo, BorderLayout.NORTH);
        cabecera.add(resultadoDetalle, BorderLayout.CENTER);
        JPanel secretos = Tema.panel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        secretos.add(secretoFinal);
        secretos.add(secretoFinalM1);
        secretos.add(secretoFinalM2);
        cabecera.add(secretos, BorderLayout.SOUTH);
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(historialResultado, BorderLayout.CENTER);
        JPanel salida = Tema.panel(new BorderLayout(10, 8));
        salida.add(guardado, BorderLayout.CENTER);
        JPanel acciones = Tema.panel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        acciones.add(reintentar);
        acciones.add(nueva);
        acciones.add(terminar);
        salida.add(acciones, BorderLayout.EAST);
        panel.add(salida, BorderLayout.SOUTH);
        return panel;
    }

    @Override public void actualizar(EstadoVistaJuego nuevoEstado) {
        boolean entrandoMenu = nuevoEstado.menu() && (estado == null || !estado.menu());
        boolean nuevaPartida = !nuevoEstado.menu() && (estado == null || estado.menu());
        estado = nuevoEstado;
        actualizando = true;
        try {
            if (entrandoMenu) usuario.setText(estado.usuario());
            if (nuevaPartida) {
                preguntas.reiniciar();
                seleccion.reiniciarSeleccion();
                tablero.reiniciarSeleccion();
            }
            menu.setVisible(!estado.menu());
            if (estado.menu()) {
                actualizarEstadisticas();
                mostrar("menu");
                situacion.setText("Elegí cómo jugar");
                pie.setText("Las estadísticas se guardan por nombre. El modo espectador tiene su propio marcador global.");
            } else if (estado.estado() == EstadoPartida.SELECCION_PERSONAJE) {
                Set<Integer> ids = estado.personajes().stream().map(Personaje::getId).collect(Collectors.toSet());
                seleccion.actualizar("Elegí tu personaje secreto", estado.personajes(), ids, true);
                actualizarSeleccion();
                mostrar("seleccion");
                situacion.setText(estado.modo().getDescripcion());
                pie.setText("Esta elección no consume un turno. Los personajes conservan sus IDs originales.");
            } else if (estado.estado() == EstadoPartida.DECISION_SEGUNDA_FASE) {
                mostrarDecision();
            } else if (estado.estado() == EstadoPartida.FINALIZADA) {
                mostrarResultado();
            } else if (estado.espectador()) {
                mostrarEspectador();
            } else {
                mostrarHumano();
            }
        } finally {
            actualizando = false;
        }
    }

    private void mostrarHumano() {
        tablero.actualizar("Tu tablero", estado.personajes(), estado.candidatosDe(Participante.JUGADOR), estado.turnoHumano());
        preguntas.actualizar(estado.fase(), estado.preguntasRealizadas(), estado.turnoHumano());
        secretoJugador.actualizar(estado.secretoJugador());
        miniTablero.actualizar(estado.rival().getNombre(), estado.personajes(), estado.candidatosDe(estado.rival()));
        historialHumano.actualizar(estado.historial());
        actualizarRiesgo();
        situacion.setText("<html>" + (estado.turnoHumano() ? "Tu turno, " + Tema.html(estado.usuario())
                : "Turno de " + estado.rival().getNombre()) + "<br>Fase " + estado.fase() + " · Ronda " + estado.ronda() + "</html>");
        pie.setText(estado.turnoHumano()
                ? "Consultá atributos con el cursor o el foco del teclado. Arriesgar ejecuta la jugada directamente."
                : "La máquina juega automáticamente después de una pausa breve.");
        mostrar("humano");
    }

    private void mostrarEspectador() {
        tableroM1.actualizar("Máquina 1", estado.personajes(), estado.candidatosDe(Participante.MAQUINA_1), false);
        tableroM2.actualizar("Máquina 2", estado.personajes(), estado.candidatosDe(Participante.MAQUINA_2), false);
        secretoM1.actualizar(estado.secretosEspectador().get(Participante.MAQUINA_1));
        secretoM2.actualizar(estado.secretosEspectador().get(Participante.MAQUINA_2));
        historialEspectador.actualizar(estado.historial());
        pausa.setText(estado.pausado() ? "Continuar" : "Pausar");
        paso.setEnabled(estado.pausado());
        velocidad.setSelectedIndex(estado.demoraMaquina() >= 1500 ? 0 : estado.demoraMaquina() <= 500 ? 2 : 1);
        situacion.setText("Ronda " + estado.ronda() + " · " + estado.turno().getNombre()
                + (estado.pausado() ? " · En pausa" : ""));
        pie.setText("Los dos secretos y el razonamiento completo están expuestos. Pausá para avanzar de a un turno.");
        mostrar("espectador");
    }

    private void mostrarDecision() {
        Participante siguiente = estado.rival() == Participante.MAQUINA_1 ? Participante.MAQUINA_2 : Participante.MAQUINA_1;
        explicacionFase.setText("<html>Ganaste antes de que el rival descartara 15 personajes.<br><br>"
                + "Si aceptás, jugarás contra <b>" + siguiente.getNombre() + "</b> y conservarás tu secreto.<br>"
                + "La nueva máquina heredará los candidatos restantes de la anterior.<br>"
                + "Tu tablero se reiniciará, excluyendo el secreto que ya adivinaste;<br>"
                + "podrás volver a hacer todas las preguntas y empezarás vos.<br><br>"
                + "Si ganás otra vez, será una victoria verdadera. Si perdés, la partida será una derrota.<br>"
                + "También podés terminar ahora y conservar esta victoria.</html>");
        situacion.setText("Primera fase superada");
        pie.setText("El resultado se registrará cuando decidas terminar o completes la segunda fase.");
        mostrar("decision");
    }

    private void mostrarResultado() {
        resultadoTitulo.setText(TextoPartida.resultado(estado.resultado()));
        resultadoDetalle.setText(estado.modo().getDescripcion() + " · " + estado.historial().size() + " acciones · "
                + estado.fase() + (estado.fase() == 1 ? " fase" : " fases"));
        secretoFinal.setVisible(!estado.espectador());
        secretoFinalM1.setVisible(estado.espectador());
        secretoFinalM2.setVisible(estado.espectador());
        secretoFinal.actualizar(estado.secretoRivalFinal());
        secretoFinalM1.actualizar(estado.secretosEspectador().get(Participante.MAQUINA_1));
        secretoFinalM2.actualizar(estado.secretosEspectador().get(Participante.MAQUINA_2));
        historialResultado.configurarEspectador(estado.espectador());
        historialResultado.actualizar(estado.historial());
        guardado.setText(switch (estado.guardado()) {
            case GUARDANDO -> "Guardando resultado…";
            case GUARDADO -> estado.espectador() ? "Resultado guardado en el marcador de máquinas." : "Resultado guardado en tus estadísticas.";
            case ERROR -> "<html>No se pudo guardar: " + Tema.html(estado.errorGuardado() == null ? "error de archivo" : estado.errorGuardado())
                    + "<br>Podés reintentar o salir sin guardar.</html>";
            case NO_CORRESPONDE -> "Resultado pendiente de guardado.";
        });
        guardado.setForeground(estado.guardado() == Guardado.ERROR ? new Color(0x75414b) : Tema.TEXTO);
        reintentar.setVisible(estado.guardado() == Guardado.ERROR);
        boolean guardando = estado.guardado() == Guardado.GUARDANDO;
        nueva.setEnabled(!guardando);
        terminar.setEnabled(!guardando);
        menu.setEnabled(!guardando);
        situacion.setText("Partida finalizada");
        pie.setText(estado.guardado() == Guardado.ERROR ? "El resultado todavía no cuenta en las estadísticas."
                : "Podés revisar el historial completo antes de iniciar otra partida.");
        mostrar("resultado");
    }

    private void actualizarEstadisticas() {
        if (estado.cargandoEstadisticas()) {
            estadisticasUsuario.setText("Consultando estadísticas…");
            estadisticasMaquinas.setText("Consultando marcador de máquinas…");
            return;
        }
        if (estado.errorEstadisticas() != null) {
            estadisticasUsuario.setText("<html>No se pudieron leer las estadísticas.<br>" + Tema.html(estado.errorEstadisticas()) + "</html>");
            estadisticasMaquinas.setText("Usá Consultar estadísticas para reintentar.");
            return;
        }
        Estadisticas e = estado.estadisticasUsuario();
        estadisticasUsuario.setText(e == null ? "Ingresá tu nombre para consultar tus estadísticas."
                : "<html><b>" + Tema.html(estado.usuario()) + "</b><br>" + e.getPartidasTotales() + " partidas · "
                        + e.getVictorias() + " victorias<br>" + e.getVictoriasVerdaderas() + " victorias verdaderas (incluidas en victorias)</html>");
        Estadisticas m = estado.estadisticasMaquinas();
        estadisticasMaquinas.setText(m == null ? "Marcador de máquinas no disponible."
                : "<html><b>Máquina 1 vs Máquina 2</b><br>" + m.getPartidasTotales() + " partidas<br>"
                        + m.getVictoriasMaquina1() + " victorias de M1 · " + m.getVictoriasMaquina2() + " victorias de M2</html>");
    }

    private void actualizarSeleccion() {
        Personaje personaje = seleccion.personajeSeleccionado();
        elegir.setEnabled(personaje != null);
        elegido.setText(personaje == null ? "Seleccioná una carta para que sea tu secreto."
                : "Tu elección: " + personaje.getNombre() + " · ID " + personaje.getId());
    }

    private void actualizarRiesgo() {
        Personaje personaje = tablero.personajeSeleccionado();
        arriesgar.setEnabled(estado != null && estado.turnoHumano() && personaje != null);
        candidato.setText(personaje == null ? "Seleccioná una carta para arriesgar."
                : "Seleccionado: " + personaje.getNombre() + " · ID " + personaje.getId());
    }

    private void mostrar(String pantalla) {
        if (!pantallaActual.equals(pantalla)) {
            cancelarEfectos();
            pantallas.show(contenido, pantalla);
            pantallaActual = pantalla;
        }
        if (!pantalla.equals("resultado")) menu.setEnabled(true);
    }

    private void cancelarEfectos() {
        seleccion.cancelarEfectos();
        tablero.cancelarEfectos();
        tableroM1.cancelarEfectos();
        tableroM2.cancelarEfectos();
    }

    @Override public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo realizar la acción", JOptionPane.WARNING_MESSAGE);
    }

    @Override public boolean confirmarAbandono() {
        return JOptionPane.showConfirmDialog(this, "¿Abandonar esta partida? No contará en las estadísticas.",
                "Abandonar partida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override public boolean confirmarSalidaSinGuardar() {
        return JOptionPane.showConfirmDialog(this, "El resultado no se guardó. ¿Salir sin registrarlo en las estadísticas?",
                "Salir sin guardar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override public void cerrar() {
        alCerrar.accept(usuario.getText());
        cancelarEfectos();
        dispose();
    }

    private static JPanel vertical() {
        JPanel panel = Tema.panel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JPanel tarjeta(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Tema.BORDE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        return panel;
    }
}

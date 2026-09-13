# Bitácora de desarrollo

## 2026-09-09 - Paso 1 del plan de acción

Pedido: incorporar el ordenamiento inicial exigido, mantener la documentación
al avanzar el proyecto y quitar del README el apartado que explica la carpeta build.

Decisiones e implementación:

- Reemplazar la inserción ordenada por el agregado al final y un MergeSort estable
  explícito sobre nodos enlazados. Ordenar una vez después de seleccionar y cargar
  los 23 personajes.
- Conservar los ID, la selección de personajes, las referencias a objetos y los
  índices de búsqueda.
- Documentar los contratos, Divide y Conquista, la complejidad y los pendientes
  del informe.
- Quitar «Qué es la carpeta build» del README y conservar las instrucciones de ejecución.

Problema resuelto: aplicar MergeSort después de una inserción ya ordenada duplicaría
el trabajo y mantendría la construcción cuadrática de la lista. Agregar al final
elimina ese recorrido. El costo de copiar el arreglo de ID se mantiene separado
y se documenta explícitamente.

Verificación realizada:

- `javac -encoding UTF-8 -Xlint:all -d build`, con todos los archivos fuente y de
  pruebas: finalizó sin errores ni advertencias.
- Pasaron los seis programas de regresión, incluidas las nuevas pruebas de
  ordenamiento estable, 100 mazos sorteados, protección de secretos, manejo de
  entradas, transiciones de fase y diagnóstico de estrategias sobre 22.000 tableros.

Herramientas y asistencia: implementación y documentación preparadas con asistencia
de IA mediante Codex a pedido del usuario; PowerShell, Python para editar archivos
conservando su codificación, compilador y entorno de ejecución Java, y Git para
inspección local. Se recuperaron el plan de acción anterior y la auditoría de
documentación desde la conversación del proyecto.
El primer intento de edición no encontró `python` en PATH y no produjo cambios.
Luego se utilizó correctamente el intérprete incluido en el entorno instalado.

El equipo debe registrar la atribución de sus aportes y la revisión humana final.
No se infieren nombres de participantes ni fechas de trabajos anteriores.

Corrección documental: a pedido del usuario, el informe, la bitácora y los mensajes
de la nueva prueba se pasaron al español. La documentación y los comentarios del
código deben mantenerse en español en los pasos siguientes.

Próximo paso: comparación experimental con un ordenamiento cuadrático, usando los
mismos 23 personajes en el mismo orden inicial. Todavía no se presentan resultados
de mediciones.

## 2026-09-10 - Paso 3: resultados y persistencia

Alcance aprobado por el usuario: resultado final único, estadísticas por usuario,
archivo local, partidas totales y marcador global con victorias de cada máquina.
El usuario decidió omitir el paso 2. Captura de nombre y marcador visual siguen
previstos para Swing, sin agregar pantallas de consola.

Implementación asistida por Codex: resultado inmutable con identificador, retorno
del ganador de fase 2, servicio de registro y consulta, contrato de repositorio y
archivo Properties UTF-8 con reemplazo atómico. Se conservan entradas por partida
para deduplicar incluso después de recargar; los totales se calculan del registro.
La carpeta data queda excluida de Git. No se agregaron dependencias.

Verificación: compilación con UTF-8 y -Xlint:all; seis regresiones existentes y dos
nuevas pruebas de estadísticas y resultados. Cobertura de ambos órdenes de fase,
derrota en segunda fase, ganadores de máquinas, reinicio, nombres con acentos,
recarga, duplicados, archivo inválido y reintento tras fallo simulado de guardado.
Herramientas: PowerShell, apply_patch, javac, java y lectura del historial de la tarea.
## 2026-09-10 - Paso 4: separar lógica y presentación

Alcance aprobado: motor por acciones sin consola, adaptador de consola, resultados
estructurados de turno, consultas protegidas, pruebas por API pública y comentarios
breves en español en los bloques complejos. Swing continúa en el paso 5.
Al retomar la tarea se verificó que no había modificaciones iniciadas.

Implementación:

- `Partida` conserva estado, fase, ronda y turno; valida las acciones antes de
  modificar el juego. Se eliminan Scanner, impresiones y el bucle de partida completa.
- `PartidaConsola` lee opciones y presenta respuestas y decisiones. `MenuConsola`
  abre este adaptador. El fin de entrada deja una partida incompleta sin resultado.
- `ResultadoTurno` y `DiagnosticoTurno` conservan datos inmutables de la acción:
  riesgo, sorteo, comparaciones, empates, respuesta o intento y descartes. Las
  estrategias dejan de imprimir; la consola no repite la decisión aleatoria.
- Las consultas de personajes, candidatos y preguntas devuelven copias inmutables.
  Los secretos de máquinas solo pueden consultarse en modo espectador.
- La segunda fase mantiene el UUID y el secreto humano, copia independientemente
  el tablero heredado, excluye el secreto anterior y reinicia preguntas y ronda.
  La decisión pendiente no genera un resultado definitivo.
- Los constructores aceptan fuentes aleatorias separadas para reproducir secretos
  y decisiones. Las pruebas de partida ya no alteran campos privados con reflexión.
- Se comentaron validaciones previas a la mutación, transiciones de fase, herencia,
  protección de consultas y conservación del diagnóstico anterior al filtrado.
  README e informe técnico incluyen contratos, cambios de API y complejidades.

Verificación realizada:

- Antes de editar: compilación y ocho programas de regresión originales aprobados.
- Después: compilación UTF-8 con `-Xlint:all`, sin errores ni advertencias, y nueve
  programas de regresión aprobados. Se conserva cobertura de 1.440 búsquedas,
  22.000 tableros, ambos órdenes de fase y límites de 0, 14, 15 y 22 descartes.
- Pruebas del motor con entrada estándar que falla al leer y captura de salida;
  estados, turnos, consultas inmutables y rechazos sin efectos comprobados.
- Flujos de consola con entradas inválidas, preguntas agotadas, reanudación,
  aceptación/rechazo de desafío y victoria verdadera. Ejecución desde `main.Main`
  de partida espectador completa, regreso al menú y cierre por fin de entrada humano.

No se modificaron las fórmulas de estrategia, el catálogo, MergeSort ni la
persistencia. Cambiaron las API de partida y máquina y la organización de los
mensajes de consola. La integración del marcador y la captura de usuario siguen
pendientes para Swing. No se realizaron commits ni publicación remota.

Herramientas: asistencia de Codex, PowerShell con escritura UTF-8 explícita,
apply_patch, javac, java y consultas de Git. La revisión final no detectó errores
de espacios en el diff; las verificaciones no requieren dependencias nuevas.

## 2026-09-12 - Paso 5: implementación e integración de Swing

El usuario aprobó la implementación después de revisar el flujo y el boceto.
Al retomar una interrupción se conservaron los cambios útiles del controlador,
la consulta final de secreto y los componentes visuales iniciados, y se continuó
la integración sobre ellos.

Decisiones de interfaz aprobadas:

- Una ventana con pantallas internas, Swing por defecto y consola con `--consola`.
- Fondo celeste pastel, paleta `#a6e1ff`, `#fff2b2` y `#b3747e`, cartas compactas
  y desplazamiento vertical. Los atributos se consultan al pasar el puntero o
  enfocar una carta; el intento seleccionado se ejecuta sin otra confirmación.
- Preguntas agrupadas por atributo, con acción explícita de preguntar y bloqueo
  de preguntas repetidas según el estado del motor.
- Tablero propio y resumen rival en modo humano. Dos tableros, secretos visibles
  y razonamiento expuesto en modo espectador, con pausa, velocidad y siguiente turno.
- Transiciones sencillas, sin sonido. Integración de los sprites locales aportados
  por el usuario, en lugar de los placeholders planteados inicialmente.
- Confirmación de abandono, resultado con secreto rival revelado y reintento o
  salida confirmada sin guardar cuando falla la persistencia.

Implementación:

- `ControladorJuego` conecta `IPartida`, `IVistaJuego` y `ServicioEstadisticas`.
  La GUI recibe instantáneas inmutables; el historial captura fase y ronda antes
  de cada transición. Las decisiones y fórmulas permanecen en el motor.
- El EDT atiende la interacción y un temporizador de una ejecución avanza las
  máquinas. Los tokens invalidan eventos cancelados, incluso si ya estaban
  encolados. La navegación cancela el reloj antes de abrir un diálogo modal.
- Un ejecutor de un solo hilo serializa el acceso al archivo fuera del EDT.
  Cada guardado conserva usuario, resultado y sesión; las consultas obsoletas y
  las respuestas posteriores al cierre se descartan. Se mantiene la deduplicación
  por UUID y las partidas incompletas no se registran.
- `getSecretoRivalFinal` permite revelar el objetivo actual solo después de una
  partida humana finalizada. Se conservan las protecciones del modo espectador.
- Los recursos se organizan por ID estable en `resources/personajes`. Se preservan
  los originales y se resuelve expresamente la numeración del sprite de Gael
  como ID 22, manteniendo a Pablito Lescano como ID 23.
- Se documentan las responsabilidades reales de cada capa, el manejo de errores
  y los costos de instantáneas, historial y persistencia. El paso 2 permanece
  omitido por decisión del usuario, sin nuevas mediciones ni cambios de reglas.

Verificación realizada:

- Compilación `--release 17`, UTF-8 y `-Xlint:all`, sin errores ni advertencias.
  Ejecución con JDK 25 de trece regresiones: nueve originales y cuatro nuevas
  para revelación final, controlador y Swing.
- Casos de cancelación de turnos, pausa, velocidad, ambas segundas fases, snapshots,
  guardado/reintento idempotente, errores de lectura y respuestas tardías.
- Eventos de botones en una ventana real creada en el EDT, pintada sin mostrarla
  y con estadísticas en memoria. Capturas de menú, selección, partida, desafío,
  resultados, espectador y error de guardado; revisión adicional del tamaño mínimo.
- Correcciones surgidas de la revisión: grilla sin estirar cartas ni perder columnas
  al redimensionar, texto del menú sin recortes y descripción precisa de la
  estrategia de Máquina 1. El espectador mantiene el razonamiento expuesto.
- SHA-256 idéntico de los 36 sprites y carga desde carpeta y classpath; validación
  UTF-8, entrada de consola, manejo de ejecución sin pantalla y `git diff --check`.

Las pruebas no escribieron estadísticas del usuario. No se agregaron dependencias
ni se realizaron commits o publicaciones remotas.

Herramientas: asistencia de Codex, PowerShell con lectura UTF-8 explícita,
apply_patch y consultas de Git. La implementación utiliza la biblioteca estándar
de Java y recursos locales.

## 2026-09-13 - Miniatura de candidatos del rival

Se aprobó conservar el secreto propio arriba, incorporar la miniatura en el medio
y reducir el historial debajo, manteniendo su desplazamiento y el razonamiento
ampliable. `PanelMiniTablero` presenta los 23 retratos en seis columnas, con gris
y cruz para descartados, posiciones estables y datos consultables con el cursor.
Se actualiza desde los candidatos del rival actual de la instantánea, incluidos
los heredados en la segunda fase. No requiere cambios en el motor o controlador.

Pasaron la compilación completa con UTF-8, `--release 17` y `-Xlint:all`, las dos
regresiones Swing y la regresión del controlador. Se comprobaron ambos rivales,
los descartes tras cada tipo de turno, la segunda fase y una nueva partida.
Se revisaron las capturas a 1366 × 768 y 980 × 660; para conservar espacio legible
en el historial mínimo se quitó una línea de estado repetida en la cabecera.
Las pruebas usaron estadísticas en memoria. Se preservaron los cambios previos.

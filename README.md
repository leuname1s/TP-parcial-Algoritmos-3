# TP Adivina Quién

Juego en Java con interfaz Swing y consola opcional, con un catálogo de 36 personajes. Cada partida selecciona
23 personajes al azar, sin repetición, y conserva sus ID originales y el mismo
mazo durante ambas fases. Los personajes se almacenan en una lista enlazada
ordenada por género mediante MergeSort estable al preparar el mazo.

## Estructura actual del proyecto

```text
source/                  Código fuente del juego (.java)
    main/                Punto de entrada: Main.java
    defaults/            Catálogo de personajes y selección del mazo
    datos/               Personajes, mazo, nodos y enumeraciones
    Funcionalidades/     Menú, partida, máquinas y tableros de candidatos
    Controladores/       Coordinación de Swing, turnos e integración de estadísticas
    GUI/                 Ventana, pantallas y componentes Swing
    Interfaces/          Contratos que implementan las clases
resources/personajes/    Sprites PNG identificados por ID estable
docs/                    Informe técnico en progreso y bitácora
tests/                   Código fuente de las pruebas automatizadas (.java)
build/                   Archivos compilados del juego y las pruebas (.class)
README.md                Descripción e instrucciones del proyecto
.gitignore               Reglas para excluir archivos generados de Git
```

| Carpeta | Responsabilidad y clases principales |
|---|---|
| `source/main` | `Main` abre Swing por defecto y la consola con `--consola`. |
| `source/defaults` | `CatalogoPersonajes` define los 36 personajes, asigna sus ID en orden de incorporación y sortea los 23 de cada partida. |
| `source/datos` | `Personaje` contiene los atributos de un personaje. `MazoPersonajes` administra la colección con una lista enlazada e índice por ID; también se reutiliza con capacidad 36 para construir el catálogo. `Nodo` es un elemento de esa lista. `Genero`, `ColorPelo` y `ModoJuego` enumeran las opciones disponibles. `Pregunta` define las preguntas y cómo evaluar cada una. |
| `source/Funcionalidades` | `MenuConsola` permite elegir el modo de juego. `Partida` coordina la selección de secretos, los turnos y las fases. `Maquina1` y `Maquina2` usan `EstrategiaMaquina` para compartir los cálculos y la ejecución del turno con comportamientos diferentes. `TableroCandidatos` lleva los descartes de cada participante. `PartidaConsola` concentra la interacción de la partida por consola; el motor y las estrategias no leen ni imprimen. |
| `source/Interfaces` | Define los métodos que deben ofrecer las implementaciones, por ejemplo `IPartida`, `IMaquina` e `ITableroCandidatos`. `IArbitroTurno` permite a las máquinas consultar respuestas y comprobar intentos sin recibir el personaje secreto rival. Estas interfaces son contratos de Java, no pantallas gráficas. |
| `source/Controladores` | `ControladorJuego` solicita acciones al motor, programa turnos automáticos y registra resultados. Entrega a la vista un `EstadoVistaJuego` inmutable y conserva el historial con fase y ronda. |
| `source/GUI` | `VentanaPrincipal` presenta las pantallas en una sola ventana. Los componentes dibujan cartas, preguntas e historial y delegan las acciones al controlador. |
| `resources/personajes` | Los 36 PNG locales se cargan por ID (`01.png` a `36.png`), independientemente del nombre del personaje. |
| `tests` | Programas de regresión para el motor, persistencia, controlador y presentación. Se ejecutan por separado del juego. |

Al ejecutar el juego, `Main` crea la ventana y `ControladorJuego`, que conecta
la vista con `Partida` y `ServicioEstadisticas`. Con `--consola`, abre
`MenuConsola` y el adaptador `PartidaConsola`. El motor obtiene
el mazo de `CatalogoPersonajes`, controla turnos y fases y devuelve los resultados
que cada presentación muestra. Las preguntas filtran el tablero correspondiente.

El **catálogo** contiene todos los personajes definidos; el **mazo** contiene los
23 elegidos para esa partida; cada **tablero** registra cuáles de esos personajes
siguen siendo candidatos para un participante. Los descartes no eliminan
personajes del catálogo ni del mazo.

## Compilar y ejecutar

Se necesita un JDK 17 o posterior con los comandos `javac` y `java` disponibles.
La interfaz requiere un entorno gráfico. Ejecutar desde
la carpeta raíz del repositorio en PowerShell:

```powershell
New-Item -ItemType Directory -Path build -Force | Out-Null
$gameSources = @(Get-ChildItem -Path source,tests -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)
javac -encoding UTF-8 -Xlint:all -d build @gameSources
if ($LASTEXITCODE -ne 0) { throw 'La compilación falló' }
java -cp build main.Main
```

Estos comandos crean la carpeta de salida si hace falta, reúnen los archivos
fuente del juego y las pruebas, los compilan y abren el menú principal.
`-encoding UTF-8` permite leer correctamente los acentos del código y
`-Xlint:all` activa las advertencias del compilador.

Para usar la consola: `java -cp build main.Main --consola`. Los sprites se buscan
en `resources/personajes` desde la raíz del proyecto; también se admite cargar
`/personajes/` desde el classpath. Por ejemplo, en Windows:
`java -cp "build;resources" main.Main`. Si falta una imagen, se muestra la inicial
del personaje y la partida sigue disponible. No se necesita conexión a Internet.

## Ejecutar las pruebas

Después de compilar, ejecutar:

```powershell
java -cp build OrdenamientoMazoRegressionTest
java -cp build PersonajesMazoRegressionTest
java -cp build EntradaJugadorRegressionTest
java -cp build SecretosRegressionTest
java -cp build Funcionalidades.EstrategiasPartidaRegressionTest
java -cp build Funcionalidades.DiagnosticoMaquinasRegressionTest
java -cp build EstadisticasRegressionTest
java -cp build ResultadosRegressionTest
java -cp build MotorPartidaRegressionTest
java -cp build RevelacionFinalRegressionTest
java -cp build Controladores.ControladorJuegoRegressionTest
java -cp build GUI.InterfazSwingRegressionTest
java -cp build GUI.FlujoSwingRegressionTest
```

| Prueba | Qué verifica |
|---|---|
| `OrdenamientoMazoRegressionTest` | Orden y estabilidad, listas de 0 a 36 elementos, identidad e ID, ordenamiento repetido, agregado posterior y preparación de 100 mazos reproducibles. |
| `PersonajesMazoRegressionTest` | Los 36 perfiles distinguibles, el sorteo de mazos, los ID no consecutivos, los filtros y la independencia de los tableros. |
| `EntradaJugadorRegressionTest` | El rechazo de preguntas repetidas y de ID ausentes o descartados, el menú cuando se agotan las preguntas y el reinicio de partida. |
| `SecretosRegressionTest` | La protección de secretos, las búsquedas de las máquinas, ambos órdenes de la segunda fase y partidas entre máquinas. |
| `Funcionalidades.EstrategiasPartidaRegressionTest` | Los límites de riesgo, los descartes heredados, las preguntas adaptativas, los desempates, una acción por turno y la segunda fase con 14/15 descartes, rechazo del desafío y derrota inicial. |
| `Funcionalidades.DiagnosticoMaquinasRegressionTest` | La selección equilibrada en 22.000 tableros, incluidos casos de pelo rubio con división 5/18, y el registro de comparación de preguntas, desempates y riesgo de ambas máquinas. |
| `EstadisticasRegressionTest` | Registro, UTF-8, recarga, duplicados, datos inválidos y reintento de guardado. |
| `ResultadosRegressionTest` | Derrota en segunda fase, ambos ganadores de máquinas, resultado estable y nuevo UUID al reiniciar. |
| `MotorPartidaRegressionTest` | Ausencia de entrada/salida en el motor, orden de acciones, consultas inmutables y rechazos sin cambios de estado. |
| `RevelacionFinalRegressionTest` | Secreto rival inaccesible antes del resultado, ambos órdenes de segunda fase y protección después de reiniciar. |
| `Controladores.ControladorJuegoRegressionTest` | Coordinación de acciones, cancelación de turnos, historial y persistencia asíncrona con reloj y tareas controlables. |
| `GUI.InterfazSwingRegressionTest` | Recursos por ID, cartas, selección, descartes, preguntas por categoría y razonamiento visible en espectador. Funciona sin pantalla. |
| `GUI.FlujoSwingRegressionTest` | Eventos sobre la ventana real: ambos rivales, desafío, intento, resultado, reintento de guardado, pausa, velocidad y partida completa entre máquinas. Requiere entorno gráfico; no muestra la ventana ni usa estadísticas del usuario. |

Cada programa muestra un mensaje `PASS` si termina correctamente. Una condición
incumplida produce un error de prueba.

La prueba de ventana puede guardar capturas de los componentes reales con
`java -cp build GUI.FlujoSwingRegressionTest data/validacion-swing`. Esa carpeta
está excluida de Git junto con los datos locales. Si no hay entorno gráfico,
esa prueba informa `SKIP`; el resto puede ejecutarse igualmente.

## Reglas actuales de personajes y turnos

- Atributos: género, pelo (negro, rubio, pelirrojo o pelado), lentes, barba y falta de un diente.
- `CatalogoPersonajes` asigna ID del 1 al 36 en orden de incorporación. El sorteo no los renumera.
- Los datos del catálogo están incluidos en Java; el juego no necesita Excel para ejecutarse.
- El jugador solo puede arriesgar personajes presentes y todavía activos en su tablero.
- El jugador no puede repetir la misma pregunta dentro de una fase. Las entradas rechazadas no consumen turno.
- Las preguntas vuelven a estar disponibles al comenzar otra fase o una nueva partida.
- Los secretos de las máquinas son distintos, también entre fases. En la segunda fase, el jugador tiene 22 candidatos porque se excluye el secreto de la máquina anterior. La siguiente máquina hereda una copia independiente de los candidatos restantes de la primera.
- La segunda fase se ofrece únicamente si el humano gana y la máquina derrotada descartó menos de 15 personajes. Con 15 o más, la partida finaliza con la victoria de la primera fase.
- Cada máquina calcula sus descartes como cantidad del mazo menos candidatos vivos. Cuentan las preguntas, los intentos fallidos y los descartes heredados.
- Máquina 1 arriesga con una probabilidad de `min(100%, 4% + 5 puntos porcentuales por descarte)`; Máquina 2 usa `min(100%, 4% + 2,5 puntos porcentuales por descarte)`. Es la probabilidad de decidir arriesgar, no la de acertar. La segunda máquina aplica su propia fórmula al tablero heredado.
- Si no arriesga, Máquina 1 elige la pregunta más desbalanceada y Máquina 2 la más equilibrada. Se cuentan las respuestas sí/no sobre los candidatos vivos y se compara su diferencia absoluta: la primera maximiza esa diferencia y la segunda la minimiza. Se ignoran preguntas constantes y los empates se resuelven al azar.
- Al arriesgar, todos los candidatos vivos tienen la misma probabilidad de ser elegidos. Un intento fallido descarta únicamente al personaje elegido.
- Con un único candidato, la máquina lo arriesga obligatoriamente; si no hay preguntas útiles, también debe arriesgar. Preguntar y arriesgar consumen turnos separados, incluso cuando una pregunta deja un solo candidato.
- El registro de ambas máquinas explica el cálculo de riesgo y la decisión de preguntar o arriesgar. Al preguntar, muestra la división sí/no de cada pregunta, las preguntas constantes que ignora, la mejor diferencia, los desempates y cuántos personajes descartaría con cada respuesta. Al arriesgar, explica la elección aleatoria entre candidatos vivos. Las decisiones consultan al árbitro sin recibir el personaje secreto rival.

## Ordenamiento inicial y documentación técnica

`MazoPersonajes.agregar` conserva el orden de carga y enlaza cada nodo al final.
`CatalogoPersonajes.crearMazo` mezcla los 36 personajes con Fisher–Yates, carga
los primeros 23 y llama una vez a `ordenarPorGenero` antes de devolver el mazo.
MergeSort divide la lista, ordena ambas mitades y las combina por género:
femenino primero, masculino después. Ante géneros iguales conserva el orden
del sorteo. No cambia los personajes, sus ID ni el índice de búsqueda.

El ordenamiento cuesta `O(n log n)` y usa `O(log n)` de pila recursiva, con
mezcla iterativa y reutilización de nodos. Enlazar cada nodo al final cuesta
`O(1)`; el costo de ampliar el índice por ID se analiza por separado.
Una lista vacía o de un elemento es un caso base. Si se agregan personajes
posteriormente, hay que volver a ordenar antes de recorrerla por género.

El [informe técnico en progreso](docs/technical-report.md) registra la decisión,
las estructuras, los contratos y las complejidades de este paso. La
[bitácora](docs/development-log.md) registra el trabajo y el uso de herramientas.
La comparación experimental con un algoritmo cuadrático corresponde al paso 2;
no se afirma una ventaja de tiempo para solo 23 personajes sin medirla.

## Resultados y estadísticas (paso 3)

Cada partida terminada conserva un `ResultadoPartida` inmutable, accesible por
`IPartida.getResultado()`. Antes de terminar devuelve `null`. Iniciar otra partida
borra el resultado anterior y genera otro identificador. Las acciones sobre una
partida terminada se rechazan sin modificar su resultado. Mientras se espera la
decisión de segunda fase, el resultado sigue siendo `null`.

| Desenlace humano | Partidas totales | Victorias | Victorias verdaderas |
|---|---:|---:|---:|
| Pierde la primera o la segunda fase | +1 | +0 | +0 |
| Gana la primera y termina (rechazo o 15 o más descartes) | +1 | +1 | +0 |
| Gana ambas fases | +1 | +1 | +1 |

El marcador global de máquinas contabiliza por separado partidas totales,
victorias de Máquina 1 y victorias de Máquina 2. No afecta a usuarios humanos.
Las partidas incompletas no tienen resultado registrable.

`ServicioEstadisticas` registra resultados y permite consultar usuarios o máquinas.
`RepositorioEstadisticasArchivo` usa `data/estadisticas.properties` por defecto;
su constructor acepta otra ruta. Solo requiere la biblioteca estándar de Java.
Los nombres se recortan en los extremos, no pueden estar vacíos y distinguen
mayúsculas. No hay autenticación de usuarios.

El archivo UTF-8 conserva una entrada por identificador de partida y los contadores
se calculan al consultar. Un registro idéntico devuelve `false` sin incrementar;
el mismo identificador con otro usuario o desenlace se rechaza. La protección
persiste al cerrar y abrir el programa. Un archivo ausente representa un registro
nuevo; datos inválidos o vacíos generan un error y se conservan. El guardado usa
un temporal y reemplazo atómico: si el sistema no lo admite, informa el fallo.
El resultado sigue disponible para reintentar. Se admite una instancia local;
no hay coordinación de escrituras entre procesos.

Swing captura el usuario, consulta los marcadores y guarda automáticamente una
partida al finalizar. **La consola produce el resultado, pero no
lo guarda automáticamente.** El servicio se puede usar así después de terminar:

```java
ServicioEstadisticas servicio = new ServicioEstadisticas(new RepositorioEstadisticasArchivo());
servicio.registrar("Ana", partida.getResultado());
Estadisticas marcador = servicio.consultarUsuario("Ana");
// Para modo máquina contra máquina: registrar(null, resultado) y consultarMaquinas().
```

El paso 2 (comparación experimental y evidencia algorítmica) fue omitido por
pedido del usuario. No se realizaron mediciones de rendimiento.

## Separación de lógica y presentación (paso 4)

`Partida` implementa `IPartida` sin depender de `Scanner`, `System.in/out`,
Swing ni del servicio de estadísticas. `PartidaConsola` interpreta entradas,
invoca operaciones y muestra resultados: reúne los roles de controlador y vista
para la consola. La interfaz Swing usa el mismo motor.

| Estado | Operaciones que permiten avanzar |
|---|---|
| `SIN_INICIAR` | `iniciar(modo)`. |
| `SELECCION_PERSONAJE` | `seleccionarPersonaje(id)` o `seleccionarPersonajeAleatorio()`. |
| `EN_CURSO` | En turno humano: `preguntar(pregunta)` o `arriesgar(id)`. En turno de máquina: `ejecutarTurnoMaquina()`. |
| `DECISION_SEGUNDA_FASE` | `decidirSegundaFase(aceptar)`. |
| `FINALIZADA` | Consultar el resultado o iniciar otra partida. |

`iniciar` también permite reiniciar desde los demás estados. Un modo inválido no
modifica la partida existente. Las operaciones rechazan datos inválidos mediante
`IllegalArgumentException` y acciones fuera de estado o turno mediante
`IllegalStateException`. La consola transforma esos errores de entrada en mensajes;
el motor conserva las validaciones aunque se use otra presentación.

Cada operación válida resuelve una sola acción. Preguntar no arriesga
automáticamente ni ejecuta al rival: la presentación solicita luego el turno
correspondiente. `getTurno()` devuelve `null` cuando no hay turno activo;
`getFase()` y `getRonda()` permiten presentar el avance sin contarlo en la vista.

`ResultadoTurno` es inmutable y contiene pregunta/respuesta o personaje intentado,
acierto, descartes y candidatos restantes. En turnos de máquina también contiene
un `DiagnosticoTurno`: riesgo, sorteo, motivo de decisión, comparaciones sí/no y
preguntas empatadas. La consola redacta las explicaciones a partir de esos datos,
después de resolver el turno, sin repetir cálculos de estrategia ni sorteos.
No se acumula automáticamente un historial de turnos.

Las listas de personajes/candidatos y el conjunto de preguntas realizadas son
copias inmutables. El motor no entrega sus tableros modificables. Los antiguos
getters de objetivos se reemplazan por `getSecretoEspectador(participante)`,
disponible únicamente en modo máquina contra máquina. En modos humanos, una
pregunta devuelve la respuesta, no el personaje secreto rival.

Ejemplo de uso sin consola:

```java
IPartida partida = new Partida();
partida.iniciar(ModoJuego.JUGADOR_VS_MAQUINA_1);
partida.seleccionarPersonajeAleatorio();
ResultadoTurno respuesta = partida.preguntar(Pregunta.USA_LENTES);
ResultadoTurno rival = partida.ejecutarTurnoMaquina();
// Consultar el estado antes de solicitar la siguiente acción.
```

Cambios de API: desaparecen `Partida(Scanner)` y `IPartida.jugar()`.
El flujo completo de consola se ejecuta con `PartidaConsola.jugar(modo)`;
`IMaquina.ejecutarTurno` devuelve `ResultadoTurno` en lugar de `boolean`.
Los constructores con `Random` permiten reproducir por separado el mazo/secretos
y las decisiones de cada máquina.

La consola puede detenerse por fin de entrada sin inventar un resultado; el
adaptador permite continuar la instancia en memoria mediante `continuar()`.
Esto no guarda partidas incompletas en disco.

## Interfaz Swing e integración (paso 5)

El menú permite ingresar un nombre, consultar su marcador y elegir entre humano
contra Máquina 1, humano contra Máquina 2 o espectador. El marcador global muestra
partidas totales y victorias separadas de cada máquina. Antes de jugar, el humano
elige su secreto o pide uno aleatorio.

Las cartas conservan su posición al descartarse. Sus atributos aparecen al pasar
el cursor o recibir foco con el teclado. Seleccionar una carta habilita
`Arriesgar`; el intento se ejecuta al pulsar ese botón. Las preguntas se agrupan
por Género, Pelo, Lentes, Barba y Diente: seleccionar categoría o pregunta no
consume turno; `Preguntar` ejecuta la acción. Las preguntas usadas se deshabilitan
hasta la siguiente fase.

En partidas humanas el rival avanza automáticamente tras una pausa breve. A la
derecha, debajo del secreto propio, una miniatura muestra los 23 personajes entre
los que la máquina busca tu secreto: mantiene las posiciones y marca en gris con una
cruz los descartados. El cursor permite consultar nombre, ID y atributos. Al pasar
a la segunda fase refleja los candidatos heredados por la nueva máquina.
El historial queda debajo, más compacto y con desplazamiento; resume las acciones
y permite ampliar el razonamiento. En espectador
hay dos tableros, secretos visibles y razonamiento completo de ambas máquinas,
con pausa, velocidad y avance de un turno mientras está pausado.

La ventana utiliza fondo celeste pastel, acentos celeste, amarillo y rosa,
cartas compactas con desplazamiento vertical y efectos breves. No incluye sonido.
La referencia es 1366 × 768 y se puede redimensionar. Los sprites proporcionados
se conservaron sin alterar sus bytes; el archivo original de Gael, rotulado 23,
se asocia al ID 22 del catálogo. Pablito Lescano conserva el ID 23.

Abandonar una partida exige confirmación y no suma estadísticas. La decisión
pendiente de segunda fase tampoco registra un resultado. Al terminar se revela
el secreto del rival actual mediante una consulta permitida solo en ese estado.
El guardado se realiza fuera del hilo gráfico; mientras está pendiente se espera
su finalización antes de salir. Ante un error se puede reintentar o salir sin
guardar con confirmación. El UUID evita contar dos veces el mismo resultado.

El controlador cancela los avisos de máquina al pausar o navegar y descarta
respuestas atrasadas de consultas de estadísticas. La vista recibe copias
inmutables; no calcula probabilidades ni modifica los tableros del motor.

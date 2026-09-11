# TP Adivina Quién

Juego de consola en Java con un catálogo de 36 personajes. Cada partida selecciona
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
    Interfaces/          Contratos que implementan las clases
docs/                    Informe técnico en progreso y bitácora
tests/                   Código fuente de las pruebas automatizadas (.java)
build/                   Archivos compilados del juego y las pruebas (.class)
README.md                Descripción e instrucciones del proyecto
.gitignore               Reglas para excluir archivos generados de Git
```

| Carpeta | Responsabilidad y clases principales |
|---|---|
| `source/main` | `Main` inicia el programa y abre el menú de consola. |
| `source/defaults` | `CatalogoPersonajes` define los 36 personajes, asigna sus ID en orden de incorporación y sortea los 23 de cada partida. |
| `source/datos` | `Personaje` contiene los atributos de un personaje. `MazoPersonajes` administra la colección con una lista enlazada e índice por ID; también se reutiliza con capacidad 36 para construir el catálogo. `Nodo` es un elemento de esa lista. `Genero`, `ColorPelo` y `ModoJuego` enumeran las opciones disponibles. `Pregunta` define las preguntas y cómo evaluar cada una. |
| `source/Funcionalidades` | `MenuConsola` permite elegir el modo de juego. `Partida` coordina la selección de secretos, los turnos y las fases. `Maquina1` y `Maquina2` usan `EstrategiaMaquina` para compartir los cálculos y la ejecución del turno con comportamientos diferentes. `TableroCandidatos` lleva los descartes de cada participante. `PartidaConsola` concentra la interacción de la partida por consola; el motor y las estrategias no leen ni imprimen. |
| `source/Interfaces` | Define los métodos que deben ofrecer las implementaciones, por ejemplo `IPartida`, `IMaquina` e `ITableroCandidatos`. `IArbitroTurno` permite a las máquinas consultar respuestas y comprobar intentos sin recibir el personaje secreto rival. Estas interfaces son contratos de Java, no pantallas gráficas. |
| `tests` | Contiene nueve programas de prueba que verifican personajes y mazos, entradas del jugador, protección de secretos, estrategias, segunda fase y registro de decisiones. Se ejecutan por separado del juego. |

Al ejecutar el juego, `Main` abre `MenuConsola`. El menú crea un adaptador
`PartidaConsola`, que solicita acciones al motor `Partida`. El motor obtiene
el mazo de `CatalogoPersonajes`, controla turnos y fases y devuelve los resultados
que la consola presenta. Las preguntas filtran el tablero correspondiente.

El **catálogo** contiene todos los personajes definidos; el **mazo** contiene los
23 elegidos para esa partida; cada **tablero** registra cuáles de esos personajes
siguen siendo candidatos para un participante. Los descartes no eliminan
personajes del catálogo ni del mazo.

## Compilar y ejecutar

Se necesita un JDK con los comandos `javac` y `java` disponibles. Ejecutar desde
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

Cada programa muestra un mensaje `PASS` si termina correctamente. Una condición
incumplida produce un error de prueba.

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

La conexión del servicio al menú, la captura de usuario y la presentación del
marcador quedan para Swing. **La consola actual produce el resultado, pero no
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
para la consola. La futura interfaz Swing podrá usar el mismo motor.

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
Esto no guarda partidas incompletas en disco. La integración de estadísticas,
la captura de usuario y las ventanas siguen previstas para el paso 5.

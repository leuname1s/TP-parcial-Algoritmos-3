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
| `source/Funcionalidades` | `MenuConsola` permite elegir el modo de juego. `Partida` coordina la selección de secretos, los turnos y las fases. `Maquina1` y `Maquina2` usan `EstrategiaMaquina` para compartir los cálculos y la ejecución del turno con comportamientos diferentes. `TableroCandidatos` lleva los descartes de cada participante. Actualmente la entrada y salida por consola también están dentro de estas clases. |
| `source/Interfaces` | Define los métodos que deben ofrecer las implementaciones, por ejemplo `IPartida`, `IMaquina` e `ITableroCandidatos`. `IArbitroTurno` permite a las máquinas consultar respuestas y comprobar intentos sin recibir el personaje secreto rival. Estas interfaces son contratos de Java, no pantallas gráficas. |
| `tests` | Contiene seis programas de prueba que verifican personajes y mazos, entradas del jugador, protección de secretos, estrategias, segunda fase y registro de decisiones. Se ejecutan por separado del juego. |

Al ejecutar el juego, `Main` abre `MenuConsola`. El menú crea una `Partida`, que
obtiene un mazo de `CatalogoPersonajes` y prepara los tableros y las máquinas.
Durante los turnos, las respuestas a las preguntas permiten descartar candidatos
en el tablero correspondiente.

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
```

| Prueba | Qué verifica |
|---|---|
| `OrdenamientoMazoRegressionTest` | Orden y estabilidad, listas de 0 a 36 elementos, identidad e ID, ordenamiento repetido, agregado posterior y preparación de 100 mazos reproducibles. |
| `PersonajesMazoRegressionTest` | Los 36 perfiles distinguibles, el sorteo de mazos, los ID no consecutivos, los filtros y la independencia de los tableros. |
| `EntradaJugadorRegressionTest` | El rechazo de preguntas repetidas y de ID ausentes o descartados, el menú cuando se agotan las preguntas y el reinicio de partida. |
| `SecretosRegressionTest` | La protección de secretos, las búsquedas de las máquinas, ambos órdenes de la segunda fase y partidas entre máquinas. |
| `Funcionalidades.EstrategiasPartidaRegressionTest` | Los límites de riesgo, los descartes heredados, las preguntas adaptativas, los desempates, una acción por turno y la segunda fase con 14/15 descartes, rechazo del desafío y derrota inicial. |
| `Funcionalidades.DiagnosticoMaquinasRegressionTest` | La selección equilibrada en 22.000 tableros, incluidos casos de pelo rubio con división 5/18, y el registro de comparación de preguntas, desempates y riesgo de ambas máquinas. |

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
borra el resultado anterior y genera otro identificador; volver a llamar `jugar`
sobre una partida terminada no vuelve a ejecutarla.

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

Después de compilar, ejecutar también:

```powershell
java -cp build EstadisticasRegressionTest
java -cp build ResultadosRegressionTest
```

El paso 2 (comparación experimental y evidencia algorítmica) fue omitido por
pedido del usuario. No se realizaron mediciones de rendimiento.
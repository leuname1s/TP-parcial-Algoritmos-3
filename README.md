# TP Adivina Quién

Juego de consola en Java con un catálogo de 36 personajes. Cada partida selecciona
23 personajes al azar, sin repetición, y conserva sus ID originales y el mismo
mazo durante ambas fases. Los personajes se almacenan en una lista enlazada
ordenada por género.

## Estructura actual del proyecto

```text
source/                  Código fuente del juego (.java)
    main/                Punto de entrada: Main.java
    defaults/            Catálogo de personajes y selección del mazo
    datos/               Personajes, mazo, nodos y enumeraciones
    Funcionalidades/     Menú, partida, máquinas y tableros de candidatos
    Interfaces/          Contratos que implementan las clases
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
| `tests` | Contiene cinco programas de prueba que verifican personajes y mazos, entradas del jugador, protección de secretos, estrategias, segunda fase y registro de decisiones. Se ejecutan por separado del juego. |

Al ejecutar el juego, `Main` abre `MenuConsola`. El menú crea una `Partida`, que
obtiene un mazo de `CatalogoPersonajes` y prepara los tableros y las máquinas.
Durante los turnos, las respuestas a las preguntas permiten descartar candidatos
en el tablero correspondiente.

El **catálogo** contiene todos los personajes definidos; el **mazo** contiene los
23 elegidos para esa partida; cada **tablero** registra cuáles de esos personajes
siguen siendo candidatos para un participante. Los descartes no eliminan
personajes del catálogo ni del mazo.

## Qué es la carpeta build

`build` es la carpeta de salida de la compilación. Se creó al compilar y comprobar
el juego. El compilador `javac` transforma los archivos `.java` de `source` y
`tests` en archivos `.class`, que contienen las instrucciones que ejecuta la
máquina virtual de Java.

Por ejemplo:

```text
source/main/Main.java  --compilación-->  build/main/Main.class
```

Las subcarpetas de `build` reflejan los paquetes de Java. También pueden aparecer
archivos como `Pregunta$1.class`: son clases adicionales que genera el compilador
para implementaciones internas del código.

- Los cambios se hacen en los archivos `.java` de `source` o `tests`.
- Los archivos de `build` se generan al compilar; no se editan manualmente.
- Después de cambiar el código, hay que volver a compilar para ejecutar la versión actualizada.
- Si se elimina `build`, el código fuente se conserva. Hay que compilar nuevamente antes de ejecutar el juego o las pruebas.
- Los archivos `.class` están excluidos de Git mediante la regla `*.class` del `.gitignore`.

El nombre `build` es una elección del proyecto: en los comandos de abajo,
`-d build` indica dónde guardar el resultado de la compilación y `-cp build`
indica dónde buscar las clases para ejecutarlas.

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
java -cp build PersonajesMazoRegressionTest
java -cp build EntradaJugadorRegressionTest
java -cp build SecretosRegressionTest
java -cp build Funcionalidades.EstrategiasPartidaRegressionTest
java -cp build Funcionalidades.DiagnosticoMaquinasRegressionTest
```

| Prueba | Qué verifica |
|---|---|
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

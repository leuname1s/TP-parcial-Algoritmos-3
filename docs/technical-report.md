# Informe técnico en progreso

## Estructuras de datos y responsabilidades

- `CatalogoPersonajes` define 36 perfiles, los mezcla con Fisher–Yates y selecciona
  23 sin repetición. Ordena el mazo seleccionado antes de devolverlo.
- `MazoPersonajes` administra una lista simplemente enlazada con cabeza y cola,
  un índice directo por ID y un índice de combinaciones de atributos que rechaza
  perfiles duplicados.
- `Nodo` contiene una referencia al personaje y el enlace siguiente. El
  ordenamiento reutiliza los nodos.
- `IMazoPersonajes` expone el agregado al final y el ordenamiento estable explícito.
  La iteración oculta los nodos. Se debe ordenar después de cargar y antes de crear
  un iterador; los iteradores existentes no deben usarse tras cambios estructurales.
- Los tableros de candidatos y la selección de secretos utilizan el mazo preparado
  como antes.

El catálogo conserva el orden de declaración. Sus entradas actuales ya están
agrupadas por género; no se ordena por inserción ni se vuelve a ordenar el catálogo.

## Divide y Conquista

1. Dividir: dos referencias, una lenta y otra rápida, localizan el punto medio;
   se corta la lista en dos mitades.
2. Resolver: se ordenan recursivamente ambas mitades. Las listas vacías o de un
   elemento son los casos base.
3. Combinar: se mezclan iterativamente según `Genero.getOrden()`. Ante claves
   iguales, se toma primero el nodo izquierdo, conservando el orden relativo de
   carga dentro de cada género.

La mezcla consume cada nodo exactamente una vez. Ambas mitades se ordenan de
forma recursiva, por lo que elegir el menor nodo de sus extremos conserva el
orden. Elegir el nodo izquierdo en los empates conserva la estabilidad durante
toda la recursión. No se reescriben los ID, los objetos personaje, sus atributos,
las cantidades ni los índices de búsqueda.

La referencia a la cola permite enlazar cada personaje en tiempo constante.
El ordenamiento restablece esa cola para permitir agregados posteriores. Agregar
ya no garantiza un recorrido ordenado: quien utiliza el mazo debe invocar
`ordenarPorGenero` explícitamente cuando lo necesite.

## Complejidad y decisiones

Sean n la cantidad de personajes del mazo, c la del catálogo y m el mayor ID positivo.

| Operación | Tiempo | Espacio adicional |
|---|---|---|
| Enlazar al final de la lista | O(1) | O(1) por nodo |
| Ampliar el índice directo por ID | O(m) por ampliación | O(m) para el índice completo |
| Buscar por ID | O(1) | O(1) |
| Fisher–Yates | O(c) | O(c) para el arreglo de referencias del catálogo |
| MergeSort | O(n log n) | O(log n) de pila recursiva |
| Restablecer la cola después de ordenar | O(n) | O(1) |

El ordenamiento satisface T(n) = T(⌊n/2⌋) + T(⌈n/2⌉) + O(n).
La mezcla es iterativa para evitar una pila de profundidad lineal en esa etapa.
El algoritmo de ordenamiento no crea nodos ni arreglos de personajes adicionales.

El recorrido anterior de inserción ordenada podía acumular O(n²) de trabajo sobre
la lista. Eliminarlo evita ordenar antes de MergeSort. Esto no convierte todas
las operaciones de carga en operaciones de tiempo constante: el arreglo de ID
existente crece hasta ID + 1 y puede copiarse repetidamente, con un costo total
O(c²) para ID consecutivos del catálogo. Ese diseño del índice no cambia en este
paso y debe incluirse en un análisis completo de la inicialización. Actualmente
c = 36 y n = 23.

MergeSort se adapta a listas enlazadas, es estable y garantiza O(n log n) incluso
con muchas claves de género iguales. No hace falta incorporar también QuickSort.
El paso 2 de comparación experimental fue omitido por decisión del usuario.
No se afirma una mejora medida de tiempo para n = 23.

## Verificación

`OrdenamientoMazoRegressionTest` compara con el ordenamiento estable de referencia
de Java: entradas vacías, de un elemento, de un solo género, ordenadas e invertidas;
100 mezclas para cada tamaño de 0 a 36; conservación de identidad y búsquedas por
ID; ordenamiento repetido; agregado posterior; y orden estable exacto de 100 mazos
creados con semillas reproducibles. Los otros cinco programas de regresión cubren
el comportamiento existente del juego.

## Trabajo pendiente del informe

- Explicar las políticas voraces (Greedy) de selección de preguntas y los límites
  de su optimización local.
- Completar UML, etapas posteriores, capturas de evidencia, bibliografía,
  algoritmos no utilizados, participación del equipo y reflexión final.
- Conciliar README, documento de diseño y planilla al preparar la entrega.

No atribuir autoría al equipo ni afirmar mediciones que no estén registradas.

## Paso 3: resultado y persistencia

`Partida` conserva un resultado definitivo con UUID, modo y desenlace, resuelto
al finalizar el último enfrentamiento o rechazar el desafío opcional. Ganar ambas fases suma una victoria verdadera
incluida en las victorias; perder la segunda deja una derrota final. Una partida
completa cuenta una sola vez aunque tenga dos fases.

`ServicioEstadisticas` aplica la contabilización y depende del contrato
`IRepositorioEstadisticas`. La implementación de archivo usa Properties y UTF-8,
con una entrada por UUID (modo, desenlace y usuario). Los contadores se derivan
del registro, evitando mantener dos representaciones persistidas del mismo total.
El separador se interpreta solo dos veces, por lo que también admite nombres que
contienen `|`. Los marcadores humanos y de máquinas se consultan por separado.

La carga valida versión, identificadores, nombres y compatibilidad entre modo y
resultado antes de permitir una escritura. Un duplicado idéntico no vuelve a
contar; un duplicado contradictorio se rechaza. El guardado reemplaza el archivo
atómicamente y propaga errores, conservando el resultado para reintentar.
No se resuelve concurrencia entre procesos ni recuperación de partidas en curso.
El registro crece con las partidas: carga, consulta y guardado cuestan O(r) en
cantidad de registros (y espacio proporcional al tamaño del archivo).

La integración visual y la captura de usuario se incorporan en el paso 5.
La consola no invoca automáticamente el servicio. Las pruebas de persistencia
ejercitan directamente la API.

Paso 2: omitido por decisión del usuario; los pendientes de comparación experimental
anteriores quedan fuera del alcance actual, sin mediciones ni afirmaciones nuevas.

## Paso 4: motor por acciones y presentación de consola

La separación se basa en responsabilidades y dependencias, no en renombrar
paquetes. `Partida`, tableros y estrategias forman el núcleo del juego.
`PartidaConsola` controla la interacción y presenta los datos; `MenuConsola`
elige el modo. Se conservan los paquetes y la biblioteca estándar.

### Estado y transiciones

El motor conserva `EstadoPartida`, fase, ronda y participante de turno.
Al iniciar un modo humano genera el mazo y espera una selección válida; en modo
espectador sortea dos secretos distintos y comienza Máquina 1.

Una pregunta humana registra su respuesta y filtra candidatos; un intento
fallido descarta únicamente el ID intentado. Ambas acciones ceden el turno.
Un turno fallido del rival devuelve el turno humano y avanza la ronda.
En modo espectador la ronda avanza después de Máquina 2.

Al ganar la primera fase, el motor compara los descartes del rival con 15.
Con menos de 15 queda pendiente la decisión y no existe aún un resultado final.
Rechazar produce victoria; aceptar mantiene el UUID y el secreto humano, sortea
un secreto distinto, copia el tablero heredado y reinicia el tablero humano con
22 candidatos, las preguntas y la ronda. Ganar la segunda produce victoria
verdadera; perder cualquiera de las fases produce derrota.

Las validaciones de estado, turno, repetición de pregunta e ID se hacen antes
de modificar datos. Consultar candidatos no avanza el turno. Los datos inválidos
y las operaciones fuera de orden se distinguen mediante excepciones de argumento
y estado. Al terminar se rechazan nuevas acciones y se conserva el mismo resultado.

### Contratos y protección del estado

`IPartida` expone acciones individuales y consultas. Las consultas de colecciones
devuelven copias inmutables; no permiten descartar personajes ni modificar
preguntas por fuera de las reglas. El secreto humano sigue consultable por su
propia vista; durante una partida humana no se puede consultar el secreto rival.
El paso 5 agrega una consulta permitida únicamente al finalizar ese modo.
`IArbitroTurno` conserva la frontera entre estrategias y secreto rival.

`ResultadoTurno` distingue pregunta/respuesta de intento/acierto y conserva los
descartes y candidatos posteriores. Para las máquinas incorpora un diagnóstico
inmutable con la información usada al decidir. Las comparaciones se obtienen
sobre los candidatos anteriores al filtrado, sin consultar el secreto.
`PartidaConsola` convierte esos datos en texto después de la acción, sin volver
a invocar la estrategia. El turno único, el riesgo y el desempate no cambian.

Se sustituyen el constructor que recibía `Scanner`, el bucle `jugar()` del motor
y las consultas que exponían objetivos o tableros modificables. Las pruebas de
partida usan la API pública y fuentes aleatorias reproducibles, sin reflexión
sobre campos privados. El paso 5 utiliza estos contratos para la interfaz gráfica.

### Complejidad del motor

Sean n los personajes del mazo, p las preguntas posibles y m el mayor ID.

| Operación | Tiempo | Espacio adicional |
|---|---|---|
| Validar turno o ID y resolver un intento humano | O(1) | O(1) |
| Resolver pregunta humana | O(n) | O(1), aparte del conjunto de preguntas |
| Evaluar las preguntas de una máquina | O(p·n) | O(p) para comparaciones y empates |
| Elegir un intento aleatorio entre candidatos | O(n) | O(n) para referencias de candidatos |
| Consultar personajes o candidatos como copia inmutable | O(n) | O(n) |
| Consultar preguntas realizadas | O(p) | O(p) |
| Preparar segunda fase y copiar el tablero heredado | O(n + m) | O(n + m) |
| Conservar el resultado final | O(1) | O(1) |

El diagnóstico agrega O(p) por turno; el motor no conserva una lista creciente
de resultados. El costo de seleccionar, indexar y ordenar el mazo inicial
permanece descrito en los apartados previos. Las estrategias conservan sus
fórmulas y decisiones; no se realizaron mediciones de rendimiento.

### Verificación de la separación

Además de las regresiones de mazos, resultados y persistencia, se comprueban:
ambos órdenes de segunda fase; límites 0/14/15/22 descartes; aceptación y rechazo;
una acción por turno; conservación del secreto humano; tablero heredado
independiente; preguntas reiniciadas; resultado pendiente y final estable;
consultas inmutables; entradas inválidas; y ejecución completa de consola.

`MotorPartidaRegressionTest` reemplaza la entrada estándar por una que falla al
leer y captura ambas salidas para detectar dependencias de consola durante las
operaciones. Las pruebas de diagnóstico conservan la comparación sobre 22.000
tableros y validan los datos anteriores al filtrado y su presentación sin repetir
sorteos. Las 1.440 búsquedas ejercitan ambas estrategias con y sin herencia.

## Paso 5: implementación e integración de Swing

### Responsabilidades y navegación

`Main` abre Swing por defecto mediante el hilo de eventos (EDT); `--consola`
conserva el adaptador anterior. `VentanaPrincipal` presenta una sola ventana con
pantallas internas para menú, selección, partida, decisión de segunda fase y
resultado. El menú captura el usuario y muestra sus estadísticas y el marcador
global de partidas entre máquinas.

La separación MVC se sostiene por las dependencias: `Partida`, tableros y
estrategias resuelven las reglas; `ControladorJuego` coordina acciones, navegación,
temporización y persistencia; la GUI dibuja datos y entrega las acciones del
usuario al controlador. `IVistaJuego` define las actualizaciones y consultas que
el controlador dirige a la vista. Esta recibe `EstadoVistaJuego`, sin acceso al
motor ni a sus tableros modificables. Las colecciones de cada instantánea son
copias inmutables. `TurnoRegistrado` conserva fase y ronda anteriores a ejecutar
la acción, incluso cuando esta cambia de fase o finaliza la partida.

El controlador consulta `getSecretoRivalFinal` solo al terminar una partida
humana. El motor rechaza esa consulta en cualquier estado anterior y en modo
espectador; en este último se mantiene la consulta de ambos secretos ya existente.
La revelación no cambia reglas, candidatos, turnos ni el resultado definitivo.

### Hilo de eventos, pausas y guardado

Los eventos de Swing y las acciones del motor se coordinan en el EDT. Un
`javax.swing.Timer` de una ejecución introduce la pausa breve de la máquina en
modo humano.
En modo espectador se agregan pausa, velocidad y avance de un turno mientras está
pausado. Cada programación lleva un token: cancelar invalida también un evento
que ya estaba en la cola. Antes de confirmar una salida se cancela el reloj para
evitar que la partida avance dentro del bucle de eventos del diálogo modal.
Si se cancela la salida, se vuelve a programar cuando corresponde.

Las lecturas y escrituras de estadísticas usan un ejecutor de un solo hilo fuera
del EDT. Esto serializa las operaciones de archivo de esta ventana y evita
bloquear la interacción; no incorpora coordinación entre procesos. Las respuestas
se entregan nuevamente al EDT. Un identificador de consulta descarta estadísticas
que llegan después de cambiar de usuario; los identificadores de sesión y la
marca de cierre impiden aplicar respuestas a una partida posterior o cerrada.

Solo se registra `ResultadoPartida` al finalizar: abandonar o dejar pendiente la
decisión de segunda fase no cuenta una partida. El guardado captura resultado,
usuario y sesión, y conserva la deduplicación por UUID del servicio. Mientras se
guarda se evita cerrar la ventana; ante un error se informa la causa y se permite
reintentar o confirmar una salida sin guardar. Los errores de consulta se muestran
como errores, sin presentar contadores cero como si la lectura hubiera sido válida.

### Presentación aprobada y recursos

La referencia es una ventana de 1366 × 768 adaptable, con fondo celeste pastel
`#e4f4ff`, acentos `#a6e1ff`, `#fff2b2` y `#b3747e`, superficies blancas y texto
oscuro. Se utilizan cartas compactas con desplazamiento vertical; no se exige
mostrar los 23 personajes simultáneamente. Los atributos aparecen al pasar el
puntero o enfocar la carta con el teclado. Las cartas descartadas conservan su
posición, con imagen atenuada y estado explícito. Seleccionar una carta prepara
el intento; `Arriesgar` lo ejecuta sin una confirmación adicional.

Las preguntas se agrupan por Género, Pelo, Lentes, Barba y Diente. Elegir categoría
o pregunta no consume turno: se ejecuta con `Preguntar`. Las preguntas realizadas
se deshabilitan y vuelven a estar disponibles cuando el motor reinicia la fase.
En modo humano se muestra el tablero propio y, a la derecha, el secreto propio,
una miniatura de los candidatos del rival y el historial compacto con desplazamiento
y razonamiento ampliable. `PanelMiniTablero` dibuja seis columnas de retratos sin
acciones de selección; los descartados mantienen su lugar en gris y con una cruz.
Nombre, ID, atributos y estado se consultan al pasar el cursor. La vista utiliza
`EstadoVistaJuego.candidatosDe(estado.rival())`, por lo que presenta la herencia
de candidatos al cambiar de rival en la segunda fase sin repetir lógica del motor.
El espectador dispone de dos tableros lado a lado, ambos secretos y razonamiento
visible en el historial compartido. Las transiciones son sencillas y no se
incorpora sonido.

Los 36 sprites suministrados se cargan localmente desde `resources/personajes`,
con nombres normalizados por ID (`01.png` a `36.png`) y alternativa de carga desde
el classpath. La asociación no depende del nombre visible del personaje: el
archivo original de Gael, rotulado con 23, corresponde al ID 22 del catálogo;
Pablito Lescano conserva el ID 23. Los originales se preservan. La interfaz no
requiere acceso a DiceBear ni otras llamadas de red al ejecutar el juego.

### Costo de la integración

Con n personajes, p preguntas y h acciones registradas, construir una instantánea
copia O(n + p + h) referencias y datos de conjuntos. El historial conserva los
diagnósticos ya calculados, de hasta O(p) por acción de máquina, por lo que ocupa
O(h·p). Reconstruir tableros e historial completo con esos diagnósticos cuesta
O(n + h·p), aparte de la carga y el dibujo de imágenes. No se vuelve a ejecutar
la estrategia para presentar su razonamiento. Los participantes y las categorías
son una cantidad fija; el juego actual utiliza n = 23 y p = 9.

El acceso a estadísticas mantiene el costo O(r) del registro persistido. Sacarlo
del EDT mejora la capacidad de respuesta de la ventana, sin alterar ese costo
algorítmico ni agregar mediciones experimentales.

### Verificación del paso 5

La compilación con `javac --release 17 -encoding UTF-8 -Xlint:all` finalizó sin
errores ni advertencias; las ejecuciones se realizaron con JDK 25. Pasaron los
trece programas de regresión: nueve existentes y cuatro nuevos para revelación
final, controlador, componentes Swing y flujos de la ventana.

Las pruebas verifican eventos cancelados incluso dentro de una confirmación,
pausa/paso/velocidad, ambos órdenes de segunda fase, historial anterior a cada
transición, secreto final protegido, consultas inmutables, respuestas obsoletas,
abandono sin cómputo y errores/reintentos de guardado sin duplicar resultados.
La ventana real se creó y pintó desde el EDT sin mostrarla, con controles activados
por eventos y repositorio en memoria. Se revisaron capturas de menú, selección,
partida humana, desafío, resultado, espectador y error de guardado; también la
distribución de la partida en el tamaño mínimo de 980 × 660.

Los 36 recursos cargan desde la carpeta local y desde el classpath; se verificó
que sus SHA-256 coinciden con los originales. La entrada `--consola` permite
abrir y cerrar el menú, y la entrada sin entorno gráfico informa cómo usarla.
Se validaron UTF-8 y `git diff --check`. No se realizaron mediciones de rendimiento.

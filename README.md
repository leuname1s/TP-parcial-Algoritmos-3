# TP Adivina Quién

Juego de deducción en Java con interfaz Swing y consola opcional. El objetivo
es descubrir el personaje secreto del rival mediante preguntas sobre sus
atributos e intentos de adivinación.

Cada partida selecciona 23 personajes de un catálogo de 36, sin repetición,
y conserva sus ID y el mismo mazo durante ambas fases. El proyecto aplica
MergeSort estable para ordenar el mazo por género y criterios Greedy para
seleccionar las preguntas de las máquinas.

## Modalidades

- **Humano contra Máquina 1:** rival agresivo, con mayor probabilidad de arriesgar
  y preferencia por preguntas que dividen los candidatos de forma desbalanceada.
- **Humano contra Máquina 2:** rival con menor probabilidad de arriesgar y
  preferencia por preguntas que dividen los candidatos de forma equilibrada.
- **Máquina contra máquina:** modo espectador con ambos secretos visibles,
  explicación de las decisiones, pausa, velocidad y avance de un turno.

Swing muestra el historial de acciones, guarda las estadísticas al finalizar
y recuerda el último nombre de usuario. La consola permite jugar con el mismo
motor, pero no guarda automáticamente las estadísticas.

## Reglas principales

- Los atributos son género, color de pelo, lentes, barba y falta de un diente.
- Cada turno permite preguntar o arriesgar. Las respuestas descartan candidatos
  incompatibles; un intento fallido descarta únicamente al personaje elegido.
- El humano solo puede arriesgar candidatos activos y no puede repetir una
  pregunta dentro de la misma fase. Las entradas inválidas no consumen turno.
- Las máquinas toman decisiones sobre sus candidatos vivos sin recibir el
  secreto rival. Con un único candidato, lo arriesgan obligatoriamente.
- Si el humano gana y la máquina derrotada descartó menos de 15 personajes,
  puede enfrentar a la otra máquina. Esta hereda una copia de los candidatos
  restantes del rival anterior; el humano comienza la segunda fase con 22
  candidatos y las preguntas vuelven a estar disponibles.
- Ganar ambas fases cuenta como victoria verdadera. Perder cualquiera produce
  una derrota final; rechazar el desafío o ganar sin habilitarlo cuenta como
  victoria. Abandonar no suma estadísticas.

## Organización del repositorio

| Carpeta | Contenido |
|---|---|
| `source/` | Motor, estrategias, datos, contratos, controlador y presentaciones Swing y consola. |
| `resources/personajes/` | Los 36 sprites, asociados a los ID del catálogo. |
| `tests/` | Pruebas de regresión del motor, estadísticas, controlador e interfaz. |
| `experiments/` | Programa de comparación de MergeSort e Inserción. |
| `docs/` | Informe técnico, UML, figuras y evidencia experimental. |

Las reglas se concentran en el motor; el controlador coordina las acciones de
Swing y las vistas presentan sus resultados. Las estadísticas se gestionan
mediante un servicio separado.

## Documentación

- [Informe técnico](docs/informe-tecnico.md): arquitectura, estructuras,
  algoritmos, complejidades y verificación.
- [Comparación experimental](docs/experiments/README.md): protocolo y resultados
  de MergeSort frente a Inserción sobre los mismos 23 personajes.
- [Diagramas UML](docs/informe-tecnico.md#2-uml-de-clases-de-la-aplicación):
  clases de la aplicación, el motor y el mazo.

El informe técnico aún requiere completar los aportes individuales y la
reflexión del equipo.

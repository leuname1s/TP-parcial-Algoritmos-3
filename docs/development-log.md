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
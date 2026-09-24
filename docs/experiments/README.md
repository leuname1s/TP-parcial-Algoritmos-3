# Comparación experimental del ordenamiento

## Requisito y alcance

El PDF externo `Documentación para primer TP Programación III.pdf`, página 2,
apartado «Justificación algorítmica de Divide y Conquista», pide comparar en
milisegundos MergeSort o QuickSort con un ordenamiento cuadrático sobre la misma
lista de 23 personajes y justificar si la diferencia es significativa.

El experimento ejecuta el método real `MazoPersonajes.ordenarPorGenero()` y una
Inserción estable sobre listas simplemente enlazadas del mismo tipo `Nodo`.
La implementación experimental está en `experiments/datos/ComparacionOrdenamiento.java`;
no cambia el código del juego ni agrega dependencias. Ambos algoritmos comparan
únicamente el género, reutilizan nodos y restablecen la cola. La referencia de
Inserción recorre el prefijo ordenado desde la cabeza, sin una optimización para
insertar directamente al final: su peor caso es O(n²), incluso con claves iguales.
No representa todas las posibles variantes optimizadas de Inserción.

## Protocolo reproducible

- Se mezcla el catálogo de 36 personajes con `Collections.shuffle` y `Random`
  usando semillas 0 a 99. Se toman los primeros 23 de cada mezcla, antes de ordenar.
  No se usa `crearMazo()`, porque ya devuelve el resultado ordenado.
- Se consideran tres escenarios: mezclado, ordenado por género e invertido por
  género. En cada escenario se conserva la misma selección para cada semilla.
- Cada algoritmo recibe nodos independientes con exactamente las mismas
  referencias a personajes y el mismo orden inicial. Nunca se vuelve a medir
  una lista ya procesada; las copias se reconstruyen en cada tanda.
- Por escenario hay 15 tandas de calentamiento descartadas y 30 tandas medidas.
  Cada tanda ordena 10.000 listas: 100 semillas por 100 repeticiones.
  Se alterna el algoritmo que se ejecuta primero, incluido el calentamiento.
- `System.nanoTime()` mide cada lote completo. El tiempo por ordenamiento es
  `total_ns / 10.000 / 1.000.000`, expresado en milisegundos. Incluye el bucle de
  llamadas y el restablecimiento de la cola; excluye construcción de listas,
  índices, selección aleatoria, comprobaciones e impresión.
- Después de medir, se comprueba cada resultado contra el ordenamiento estable
  de Java: secuencia exacta por identidad, cantidad, terminación de la lista,
  cola de Inserción y búsquedas por ID del mazo. Consumir todos los resultados
  también evita dejar el ordenamiento como un cálculo sin uso observable.
- Antes de las mediciones se verifican entradas vacías, unitarias, de un solo
  género y de 36 elementos, además de ordenar dos veces el mismo resultado.

Se verifican 2.700.000 resultados de listas de 23 personajes contando ambos
algoritmos, calentamiento y medición. Las comprobaciones no tienen un umbral de
rendimiento: un tiempo mayor no indica un fallo funcional.

## Ejecución registrada: 2026-09-17

- Windows 11, versión 10.0, arquitectura amd64; 32 procesadores lógicos.
- Intel Core i9-14900HX.
- OpenJDK 64-Bit Server VM, Java `25.0.4+7-LTS`.
- Compilación con `--release 17 -encoding UTF-8 -Xlint:all`.
- JVM con `-Xms256m -Xmx256m`; una ejecución, un proceso, un hilo de medición.
- Los [datos completos](ordenamiento-2026-09-17.csv) contienen 180 filas:
  tres escenarios, 30 tandas y dos algoritmos. Se conservan los tiempos enteros
  originales en nanosegundos, el orden de ejecución y los promedios convertidos.

Cada valor de la tabla es la mediana de los 30 promedios por tanda. El intervalo
es el mínimo–máximo observado, no un intervalo de confianza.

| Entrada (23 personajes) | MergeSort: mediana (ms) | Mín.–máx. (ms) | Inserción: mediana (ms) | Mín.–máx. (ms) |
|---|---:|---:|---:|---:|
| Mezclada | 0.000630920 | 0.000491900–0.000897650 | 0.000453800 | 0.000338140–0.000678100 |
| Ordenada por género | 0.000479040 | 0.000375420–0.000860920 | 0.000482370 | 0.000413310–0.000651140 |
| Invertida por género | 0.000541280 | 0.000429800–0.000891360 | 0.000272445 | 0.000213900–0.000513500 |

## Interpretación y límites

En la entrada mezclada, Inserción tuvo una mediana menor: la diferencia fue
0.000177120 ms por ordenamiento, aproximadamente 0.177 microsegundos. En la
entrada ordenada los valores fueron muy próximos; en la invertida Inserción
también tuvo una mediana menor. Solo existen dos claves de género: invertirlas
no equivale a invertir 23 claves distintas ni asegura el peor caso de Inserción.

Para el juego, que ordena una sola lista de 23 personajes al preparar la partida,
estas diferencias no tienen relevancia práctica perceptible. Esto es una
interpretación de la magnitud medida, no una prueba estadística de significación.
Los rangos muestran variabilidad y se solapan; una ejecución en una sola JVM no
permite afirmar una superioridad universal ni aislar todos los efectos de JIT,
recolección de basura, caché, carga del sistema o temporización. El calentamiento,
los lotes y la alternancia reducen algunos sesgos, pero no los eliminan.

MergeSort conserva su justificación: implementa Divide y Conquista, es estable,
se adapta a la lista enlazada y tiene una cota O(n log n), frente al peor caso
O(n²) de la referencia por Inserción. Big O describe el crecimiento y no garantiza
menor tiempo para n = 23. No se midieron tamaños mayores, por lo que esta prueba
no demuestra experimentalmente las curvas de crecimiento. Tampoco mide el tiempo
de inicio de la aplicación ni la generación completa del mazo.

## Cómo reproducir

Desde la raíz del repositorio, en PowerShell, con un JDK 17 o posterior:

```powershell
$fuentes = @(Get-ChildItem source,experiments -Recurse -Filter *.java | ForEach-Object FullName)
javac --release 17 -encoding UTF-8 -Xlint:all -d build @fuentes
if ($LASTEXITCODE -ne 0) { throw 'Fallo de compilacion' }
java -Xms256m -Xmx256m -cp build datos.ComparacionOrdenamiento build/ordenamiento-nueva-ejecucion.csv
if ($LASTEXITCODE -ne 0) { throw 'Fallo del experimento' }
```

La salida indica entorno, medianas, mínimos, máximos y resultado de las
comprobaciones. El CSV nuevo se guarda en `build` para preservar la evidencia
registrada. Las semillas reproducen las entradas, no los tiempos exactos.

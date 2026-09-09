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
No se afirma una mejora de tiempo para n = 23 antes de la comparación experimental
del paso 2.

## Verificación

`OrdenamientoMazoRegressionTest` compara con el ordenamiento estable de referencia
de Java: entradas vacías, de un elemento, de un solo género, ordenadas e invertidas;
100 mezclas para cada tamaño de 0 a 36; conservación de identidad y búsquedas por
ID; ordenamiento repetido; agregado posterior; y orden estable exacto de 100 mazos
creados con semillas reproducibles. Los otros cinco programas de regresión cubren
el comportamiento existente del juego.

## Trabajo pendiente del informe

- Paso 2: comparación reproducible con un ordenamiento cuadrático sobre copias
  de los mismos 23 personajes en el mismo orden inicial, con medición aislada,
  calentamiento y muestras repetidas.
- Explicar las políticas voraces (Greedy) de selección de preguntas y los límites
  de su optimización local.
- Completar UML, etapas posteriores, capturas de evidencia, bibliografía,
  algoritmos no utilizados, participación del equipo y reflexión final.
- Conciliar README, documento de diseño y planilla al preparar la entrega.

No atribuir autoría al equipo ni afirmar mediciones que no estén registradas.

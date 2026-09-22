"""Verifica el contraejemplo del informe sin modificar ni ejecutar el juego."""
from functools import lru_cache
from pathlib import Path
import re

RAIZ = Path(__file__).resolve().parents[2]
texto = (RAIZ / 'source/defaults/CatalogoPersonajes.java').read_text(encoding='utf-8')
perfiles = re.findall(
    r'agregar\(catalogo, "([^"]+)", (\w+), (\w+), (true|false), (true|false), (true|false)\);', texto)
assert len(perfiles) == 36
ids = (3, 5, 7, 15, 16, 18, 20, 33)
personajes = [perfiles[i - 1] for i in ids]
preguntas = {
    'ES_FEMENINO': lambda p: p[1] == 'FEMENINO',
    'ES_MASCULINO': lambda p: p[1] == 'MASCULINO',
    'ES_CALVO': lambda p: p[2] == 'PELADO',
    'USA_LENTES': lambda p: p[3] == 'true',
    'PELO_PELIRROJO': lambda p: p[2] == 'PELIRROJO',
    'PELO_NEGRO': lambda p: p[2] == 'NEGRO',
    'PELO_RUBIO': lambda p: p[2] == 'RUBIO',
    'TIENE_BARBA': lambda p: p[4] == 'true',
    'LE_FALTA_UN_DIENTE': lambda p: p[5] == 'true',
}
mascaras = {q: sum(1 << i for i, p in enumerate(personajes) if cumple(p))
            for q, cumple in preguntas.items()}


@lru_cache(None)
def costo(conjunto):
    """Suma mínima de profundidades hasta identificar; no cuenta intento final."""
    if conjunto.bit_count() <= 1:
        return 0
    # Cada pregunta agrega una unidad a la profundidad de todos los candidatos.
    return min(conjunto.bit_count() + costo(conjunto & m) + costo(conjunto & ~m)
               for m in mascaras.values() if conjunto & m and conjunto & ~m)


total = (1 << len(ids)) - 1
resultados = {}
for pregunta, mascara in mascaras.items():
    si, no = total & mascara, total & ~mascara
    if si and no:
        resultados[pregunta] = (si.bit_count(), no.bit_count(),
                                len(ids) + costo(si) + costo(no))
equilibradas = [q for q, (s, t, _) in resultados.items() if s == t]
assert equilibradas == ['ES_CALVO']
assert resultados['ES_CALVO'] == (4, 4, 26)
assert resultados['PELO_NEGRO'] == (3, 5, 25)
assert costo(total) == 25
for q, (s, t, c) in resultados.items():
    print(f'{q}: {s}/{t}, suma={c}, promedio={c / len(ids)}')
print('OK: 3.125 < 3.25. Candidatos equiprobables, sin riesgo ni rival.')

# Quiz15sepANTLR

Angel Arcos - Juan Orjuela

Usamos las siguientes reglas, pedidas en clase

```
E -> E + T | T
T -> T * F | F
F -> id | num | (E)
```

El programa lee una expresión y dice si la gramática la acepta o no. Si la acepta, muestra el árbol, y si no, dice en qué columna está el error.

Usammos la resta (`E -> E - T`) porque los ejemplos del enunciado, como `2 + 3 - 4`, la usan, y sin esa regla se rechazaban. 

## Archivos

- `Expr.g4`: la gramática.
- `Main.java`: lee las expresiones desde la consola o desde un archivo .txt.
- `ejemplos.txt`: algunas expresiones válidas y otras inválidas para probar.

## Cómo correrlo

Desde PowerShell, en la carpeta del proyecto:

```powershell
.\construir.bat              # genera el parser y compila
.\ejecutar.bat               # modo consola
.\ejecutar.bat ejemplos.txt  # analiza un archivo
```

En el modo consola se puede escribir una expresión, la ruta de un .txt o `salir`. En los archivos, cada línea cuenta como una expresión.

## Ejemplos

```
2 + 3 * (4 - 5)  ->  ACEPTADA
2 + * 3          ->  RECHAZADA (columna 5: sobra el '*')
(2 + 3           ->  RECHAZADA (falta el ')')
```

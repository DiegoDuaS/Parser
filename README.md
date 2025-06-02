# Parser
Este proyecto implementa un generador de analizadores sintácticos basado en LR(0), utilizando gramáticas libres de contexto (GLC) especificadas en lenguaje YAPar.
Se integra con un analizador léxico generado con YALex y tiene como objetivo validar sintácticamente un archivo fuente dado. El repositorio del lex se puede encontrar aqui: [Lex](https://github.com/Fabiola-cc/Lex)

## Requisitos
- Tener instalado Maven.
- Clonar este repositorio.
- Clonar adicionalmente el repositorio [Lex](https://github.com/Fabiola-cc/Lex).
- Ejecutar el siguiente comando desde la carpeta Lex/demo/ para instalar la dependencia:

```bash
mvn clean install
```

## Estructura del Proyecto
### Modulos
#### Input
- `yalpInterpreter.java`: Se encarga de manejar el archivo de entrada ejemplo.yalp y generar la gramática.

#### Automaton
- `extension.java`: Extiende la gramática.
- `automaton.java`: Construye el autómata finito determinista (AFD).

#### Tables
- `generateParseTable.java`: Aquí se construyen las tablas de análisis sintáctico (action y goto).
- `firstpos.java`: Genera los Firstpos correspondientes.
- `followpos.java`: Genera los Followpos correspondientes.

#### Parser
- `parser.java`: Permite el analísis sintáctico del texto.
- `Traduccion_Token.java`: Permite generar listas de tokens en base a lo obtenido del lexer.

#### Error
- `Error_Handler.java`: Provee mensajes de error en caso de necesitarlos durante el analísis. 

### Modelos

- `AFD.java`: Representa el Autómata Finito Determinista
- `EstadoAFD.java`: Representa los Estados dentro del AFD
- `Grammar.java`: Representa la gramática
- `GrammarExtended.java`: Representa la gramática extendida
- `ParsingTable.java`: Representa la tabla de parseo
- `ReduceTable.java`: Representa las reducciones 

### Visualización

- `DrawAFD.java`: Crea representación visual del autómata

## Uso

1. Ingresar al archivo lexer.yal que se encuentra al archivo de resources y seguir las siguientes instrucciones:  

- Al inicio del archivo se encuentran los headers (Se pueden identificar porque se encuentran entre corchetes "{}"). Para el funcionamiento correcto del lexer, los import que se encuentran allí NO se pueden borrar  
- Se pueden definir nombres de expresiones regulares con este formato: let NOMBRE = EXPRESION REGULAR
- Para poder definir los tokens, se debe de definir un entrypoint llamado gettoken, cada regla se debe de definir con este formato: REGEX o NOMBRE { return TOKEN }. Cada uno debe de ser separado con "|".
- Al definir una expresión regular, se debe de agregar comillas simples cuando se quiera tomar como literal. "[A-Z]" -> "['A'-'Z']
- Para poder definir saltos de línea o espacios, se deben definir afuera de las reglas de token.

2. Ingresar al archivo ejemplo.yal que se encuentra al archivo de resources y seguir las siguientes instrucciones: 

- Los comentarios son delimitados por /* y */.
- La primera sección del archivo debe ser la sección de TOKENS. Cada token se define precedido por la palabra reservada %token en minúscula, seguida del nombre del token en mayúscula. Estos nombres deben coincidir exactamente con los tokens producidos por YALex.
- Una línea en esta sección puede contener la declaración de múltiples tokens, separados por un espacio en blanco.
- La palabra reservada IGNORE sirve para ignorar tokens en la ejecución del análisis sintáctico.
- Para dividir la sección de TOKENS y la sección de PRODUCCIONES, debe usarse el símbolo %%.
- En la sección de PRODUCCIONES, una producción comienza con el nombre asignado a la producción, seguido de dos puntos :. Luego se listan las distintas reglas para esa producción, separadas por el símbolo |.
- Los nombres de otras producciones (no terminales) se escriben en minúsculas y los identificadores de tokens (terminales) en mayúsculas.
- Cada producción finaliza con punto y coma ;.
- Puede haber tantas producciones como se necesiten.
- El análisis de producciones finaliza al llegar a la última línea del archivo. 

3. Puede modificar el archivo code.txt agregando cadenas de texto que puedan ser reconocidas tanto por las reglas del lexer, como del parser.
4. Ejecutar la clase Main
5. El programa: 
- Ejecutará el lexer y obtendrá los tokens resultantes
- Procesará el archivo ejemplo.yal. En base a esto obtendrá los tokens definidos y la generará la gramática a utilizar.
- Compara los tokens obtenidos del lex y del ejemplo.yal. En caso de no coincidir, terminara toda la ejecución.
- Extenderá la gramatica generada previamente.
- Generará un AFD en base a la gramatica extendida, utilizando las producciones de esta.
- Generará una visualización del AFD generado.
- Generará la tabla de parseo en 3 pasos:
  - Recorrerá el AFD, agregando a la action table si la transición viene de un terminal, agregando a la go to table en caso contrario.
  - Generará los FirstPos y FollowPos correspondientes
  - Agregara a la action table las reducciones finales, en base a los FollowPos 
- Guardara la parsing table y la lista de tokens obtenidos del lexer en un objeto.
- Generará el código de análisis llamado Yalp.java
- Compilará el código y producirá su ejecutable run.bat
- Para ejecutar el código analizador, se debe utilizar este comando: 

```bash
./run.bat 
```

## Autores 

+ Fabiola Contreras 22787
+ Diego Duarte 22075
+ María José Villafuerte 22129



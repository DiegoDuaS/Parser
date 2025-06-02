# Parser

## Descripción

**Parser** es un proyecto desarrollado en Java que integra un analizador léxico y sintáctico utilizando las herramientas **YALex** y **YAPar**. Está diseñado para que el usuario pueda definir las configuraciones necesarias y analizar la validez de cadenas contenidas en un archivo de entrada.

## Características

* **Análisis Léxico**: Utiliza YALex para definir patrones léxicos y generar tokens a partir del código fuente.
* **Análisis Sintáctico**: Emplea YAPar para construir un árbol de derivación basado en una gramática definida.
* **Extensibilidad**: La gramática puede ser modificada para soportar más estructuras y expresiones según sea necesario.

## Requisitos

* Java Development Kit (JDK) 8 o superior
* Maven

## Instalación

1. Clona y compila la dependencia:

   ```bash
   git clone https://github.com/Fabiola-cc/Lex
   cd Lex
   mvn clean install
   ```

2. Clona el repositorio principal del proyecto:

   ```bash
   git clone https://github.com/DiegoDuaS/Parser.git
   cd Parser
   ```

3. Compila el proyecto utilizando tu entorno de desarrollo preferido o desde la línea de comandos.

## Uso

1. Define la gramática en el archivo `.yalp` y los patrones léxicos en el archivo `.yal`.
2. Ejecuta el archivo `Main.java` para generar el analizador sintáctico y analizar un archivo de entrada.
3. Ejecuta el archivo `run.bat` para hacer el análisis correspondiente. El programa leerá el archivo `code.txt`, procesará su contenido y validará si cumple con la gramática definida.
4. Los resultados del análisis se mostrarán en consola.

## Estructura del Proyecto

* `lexer.yal`: Definición de los patrones léxicos.
* `parser.yalp`: Definición de la gramática para el análisis sintáctico.
* `code.txt`: Archivo de entrada con el código fuente a analizar.
* `src/`: Código fuente del proyecto.
* `README.md`: Este archivo de documentación.
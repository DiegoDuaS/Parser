import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.Modules.Analisis.Complete_Lex;
import com.example.models.Token;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.GrammarExtended.ProductionWithPointer;
import models.ParsingTable;
import modules.automaton.automatom;
import modules.automaton.extension;
import modules.input.yalpInterpreter;
import modules.parser.Parser;
import modules.tables.generateParseTable;

public class Main {
    public static void main(String[] args) {
        try {
            // Ejecutar análisis léxico
            List<Token> Lex_tokens = Complete_Lex.completeLex("code.txt", "lexer.yal");

            // Leer archivo yalp
            System.err.println("\n\n******************Análisis Sintáctico********************");
            yalpInterpreter file_reader = new yalpInterpreter();
            file_reader.readFile("ejemplo.yalp");

            Map<String, List<String>> productions = file_reader.getSavedProductions();
            System.out.println(productions);
            System.out.println();

            // Comparar tokens recibidos con los obtenidos del generador léxico
            Set<String> terminalesSet = new HashSet<>(file_reader.getSavedTokens());
            terminalesSet.removeAll(file_reader.getIgnoredTokens());
            System.out.println(terminalesSet);

            Set<String> tipos_tokens = new HashSet<>();
            for (Token token : Lex_tokens) {
                tipos_tokens.add(token.getTipo());
            }
            System.out.println(tipos_tokens);

            if (!terminalesSet.containsAll(tipos_tokens)) {
                System.err.println(
                        "Hay discrepancias entre los tokens obtenidos por el analizador léxico y los tokens definidos en el archivo yalp");
                System.err.println(
                        "Revisa tus archivos de configuración o los resultados impresos para corregirlo y volver a intentarlo");
                return;
            }
            List<String> terminales = new ArrayList<>(terminalesSet);

            List<String> noTerminales = new ArrayList<>(productions.keySet());
            System.out.println(noTerminales);
            String initialSimbol = "";
            if (noTerminales != null) {
                initialSimbol = noTerminales.get(0);
                System.out.println(initialSimbol);
            }

            Grammar grammar = new Grammar(productions, terminales, noTerminales, initialSimbol);

            // Extender la gramática
            GrammarExtended extendida = extension.extenderGramatica(grammar);

            // Crear el estado inicial del AFD
            EstadoAFD estado0 = automatom.crearEstadoInicial(extendida);
            System.out.println("\nEstado inicial:");
            System.out.println(estado0.getId());
            for (ProductionWithPointer item : estado0.getItems()) {
                System.out.println("  " + item);
            }

            // Generar AFD
            AFD afd = automatom.generarAFD(extendida, estado0);

            // Generar tablas de parseo (action + go-to)
            ParsingTable parseTable = generateParseTable.generateTables(afd, grammar);

            System.out.println("\nStep: Guardar Parsing Table");
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream("demo/src/main/resources/PARSE_TABLE.dat"))) {
                out.writeObject(parseTable);
                System.out.println("Parsing Table guardado correctamente.");
            } catch (IOException e) {
                System.err.println("Error al guardar la Parsing Table: " + e.getMessage());
            }

            // Crear parser
            Parser parser = new Parser(parseTable);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

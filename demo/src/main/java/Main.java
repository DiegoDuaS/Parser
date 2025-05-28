import java.io.IOException;
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
import models.ParsingTable;
import models.GrammarExtended.ProductionWithPointer;
import modules.automaton.automatom;
import modules.automaton.extension;
import modules.input.yalpInterpreter;
import modules.tables.generateParseTable;

public class Main {
    public static void main(String[] args) {
        try {
            // Ejecutar análisis léxico
            List<Token> Lex_tokens = Complete_Lex.completeLex("code.txt");

            // Leer archivo yalp
            System.err.println("\n\n******************Análisis Sintáctico********************");
            yalpInterpreter file_reader = new yalpInterpreter();
            file_reader.readFile("ejemplo.yalp");

            Map<String, List<String>> productions = file_reader.getSavedProductions();
            System.out.println(productions);
            System.out.println();

            // Comparar tokens recibidos con los obtenidos del generador léxico
            Set<String> terminalesSet = new HashSet<>(file_reader.getSavedTokens());
            System.out.println(terminalesSet);
            Set<String> tipos_tokens = new HashSet<>();
            for (Token token : Lex_tokens) {
                tipos_tokens.add(token.getTipo());
            }

            if (terminalesSet.containsAll(tipos_tokens))
                System.err.println(
                        "Hay discrepancias entre los tokens obtenidos por el analizador léxico y los tokens definidos en el archivo yalp");

            terminalesSet.removeAll(file_reader.getIgnoredTokens());
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

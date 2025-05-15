import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.Modules.Analisis.Complete_Lex;
import com.example.models.Token;

import models.Grammar;
import modules.input.yalpInterpreter;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

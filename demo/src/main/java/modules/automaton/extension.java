package modules.automaton;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.Grammar;
import models.GrammarExtended;

public class extension {

    public static GrammarExtended extenderGramatica(Grammar original) {
        String originalStart = original.getInitialSimbol();
        String extendedStart = originalStart + "'";

        GrammarExtended gext = new GrammarExtended(extendedStart);

        gext.agregarProduccion(extendedStart, List.of(originalStart), 0);

        for (Map.Entry<String, List<String>> entry : original.getProductions().entrySet()) {
            String lhs = entry.getKey();
            for (String prod : entry.getValue()) {
                if (prod == null) {
                    continue; // Ignora esta producción, pero sigue con las demás
                }
                List<String> rhs = Arrays.asList(prod.trim().split("\\s+"));
                gext.agregarProduccion(lhs, rhs, 0);
            }
        }

        gext.setNoTerminales(original.getNoTerminales());
        gext.setTerminales(original.getTerminales());

        return gext;
    }

    public static void main(String[] args) {
        Grammar g = new Grammar("S");
        g.agregarNoTerminal("S");
        g.agregarNoTerminal("P");
        g.agregarNoTerminal("Q");

        g.agregarTerminal("^");
        g.agregarTerminal("V");
        g.agregarTerminal("[");
        g.agregarTerminal("]");
        g.agregarTerminal("sentence");

        g.agregarProduccion("S", "S ^ P");
        g.agregarProduccion("S", "P");
        g.agregarProduccion("P", "P V Q");
        g.agregarProduccion("P", "Q");
        g.agregarProduccion("Q", "[ S ]");
        g.agregarProduccion("Q", "sentence");

        GrammarExtended extendida = extenderGramatica(g);

        for (Map.Entry<String, List<String>> entry : g.getProductions().entrySet()) {
            for (String prod : entry.getValue()) {
                System.out.println(entry.getKey() + " -> " + prod);
            }
        }

        System.out.println("EXTENDED");

        // Imprimir producciones de la extendida
        for (Map.Entry<String, List<GrammarExtended.ProductionWithPointer>> entry : extendida.getProductions()
                .entrySet()) {
            String noTerminal = entry.getKey();
            for (GrammarExtended.ProductionWithPointer prod : entry.getValue()) {
                System.out.println(noTerminal + " -> " + prod.toString());
            }
        }
    }

}

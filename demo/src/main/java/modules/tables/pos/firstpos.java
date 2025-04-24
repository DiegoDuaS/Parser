package modules.tables.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Grammar;

public class firstpos {
    private Grammar grammar;
    private Map<String, Set<String>> first = new HashMap<>();
    private List<String[]> equals = new ArrayList<>();

    public firstpos(Grammar grammar) {
        this.grammar = grammar;
    }

    public Map<String, Set<String>> calcularFirstPos() {
        for (String noTerminal : grammar.getNoTerminales()) {
            List<String> producciones = grammar.getProductions().get(noTerminal);
            if (producciones == null)
                continue;

            List<String> firstpos = new ArrayList<>();

            for (String produccion : producciones) {
                if (produccion == null) {
                    firstpos.add(null); // Considerar transiciones epsilon
                    break;
                }

                String[] symbols = produccion.trim().split("\\s+");
                if (symbols.length == 0)
                    continue;

                String firstSymbol = symbols[0];

                if (grammar.getNoTerminales().contains(firstSymbol)) {
                    if (!noTerminal.equals(firstSymbol)) {
                        equals.add(new String[] { firstSymbol, noTerminal });
                    }
                } else {
                    firstpos.add(firstSymbol);
                }
            }

            first.computeIfAbsent(noTerminal, k -> new HashSet<>()).addAll(firstpos);
        }

        for (int i = equals.size() - 1; i >= 0; i--) {
            String from = equals.get(i)[0];
            String to = equals.get(i)[1];

            Set<String> fromSet = first.get(from);
            Set<String> toSet = first.get(to);

            if (fromSet == null)
                continue;

            if (toSet == null) {
                toSet = new HashSet<>();
                first.put(to, toSet);
            }

            toSet.addAll(fromSet);
        }

        return first;
    }

    public static void main(String[] args) {
        Grammar grammar = new Grammar();

        grammar.agregarNoTerminal("S");
        grammar.agregarNoTerminal("P");
        grammar.agregarNoTerminal("Q");

        grammar.agregarTerminal("^");
        grammar.agregarTerminal("V");
        grammar.agregarTerminal("[");
        grammar.agregarTerminal("]");
        grammar.agregarTerminal("sentence");

        grammar.agregarProduccion("S", "S ^ P");
        grammar.agregarProduccion("S", "P");
        grammar.agregarProduccion("P", "P V Q");
        grammar.agregarProduccion("P", "Q");
        grammar.agregarProduccion("Q", "[ S ]");
        grammar.agregarProduccion("Q", "sentence");

        firstpos calc = new firstpos(grammar);
        Map<String, Set<String>> tablaFirst = calc.calcularFirstPos();

        for (Map.Entry<String, Set<String>> entry : tablaFirst.entrySet()) {
            System.out.println("first(" + entry.getKey() + ") = " + entry.getValue());
        }

    }

}

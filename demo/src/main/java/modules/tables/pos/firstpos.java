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

    public void calcularFirstPos() {
        for (String noTerminal : grammar.getNoTerminales()) {
            List<String> producciones = grammar.getProductions().get(noTerminal);
            if (producciones == null) continue;

            List<String> firstpos = new ArrayList<>();

            for (String produccion : producciones) {
                String[] symbols = produccion.trim().split("\\s+");
                if (symbols.length == 0) continue;

                String firstSymbol = symbols[0];

                if (grammar.getNoTerminales().contains(firstSymbol)) {
                    if(!noTerminal.equals(firstSymbol)){
                        equals.add(new String[]{firstSymbol, noTerminal});
                    }
                }
                else{
                    firstpos.add(firstSymbol);
                }
            }

            first.computeIfAbsent(noTerminal, k -> new HashSet<>()).addAll(firstpos);

        }

        for (int i = equals.size() - 1; i >= 0; i--) {
            String from = equals.get(i)[0]; // first(X)
            String to = equals.get(i)[1];   // first(Y)
        
            // Asegurarse de que existan los conjuntos
            Set<String> fromSet = first.get(from);
            Set<String> toSet = first.get(to);
        
            if (fromSet == null) continue; // nada que copiar
        
            // Crear el conjunto destino si no existe
            if (toSet == null) {
                toSet = new HashSet<>();
                first.put(to, toSet);
            }
        
            // Copiar los elementos
            toSet.addAll(fromSet);
        }

        System.out.println("\nTabla First(X):");
        for (Map.Entry<String, Set<String>> entry : first.entrySet()) {
            System.out.println("first(" + entry.getKey() + ") = " + entry.getValue());
        }
    }

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        
        grammar.agregarNoTerminal("E");
        grammar.agregarNoTerminal("E'");
        grammar.agregarNoTerminal("T");
        grammar.agregarNoTerminal("T'");
        grammar.agregarNoTerminal("F");

        // Terminales
        grammar.agregarTerminal("(");
        grammar.agregarTerminal(")");
        grammar.agregarTerminal("+");
        grammar.agregarTerminal("*");
        grammar.agregarTerminal("id");

        // Producciones (una expresión aritmética típica)
        grammar.agregarProduccion("E", "T E'");
        grammar.agregarProduccion("E'", "+ T E'");
        grammar.agregarProduccion("E'", "ε");
        grammar.agregarProduccion("T", "F T'");
        grammar.agregarProduccion("T'", "* F T'");
        grammar.agregarProduccion("T'", "ε");
        grammar.agregarProduccion("F", "( E )");
        grammar.agregarProduccion("F", "id");
    
        firstpos calc = new firstpos(grammar);
        calc.calcularFirstPos();
    }
    
}

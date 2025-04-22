package modules.tables.pos;

import java.util.List;

import models.Grammar;

public class firstpos {
    private Grammar grammar;

    public firstpos(Grammar grammar) {
        this.grammar = grammar;
    }

    public void calcularFirstPos() {
        // Recorremos la lista de no terminales
        for (String noTerminal : grammar.getNoTerminales()) {
            System.out.println("Procesando no terminal: " + noTerminal);
            
            // Obtenemos las producciones de este no terminal
            List<String> producciones = grammar.getProductions().get(noTerminal);
            
            // Recorremos las producciones del no terminal
            if (producciones != null) {
                for (String produccion : producciones) {
                    System.out.println("  Producción: " + produccion);
                }
            }
        }
    }

    public static void main(String[] args) {
        Grammar grammar = new Grammar();
    
        // Agregar no terminales
        grammar.agregarNoTerminal("S");
        grammar.agregarNoTerminal("P");
        grammar.agregarNoTerminal("Q");
    
        // Agregar terminales
        grammar.agregarTerminal("^");
        grammar.agregarTerminal("V");
        grammar.agregarTerminal("[");
        grammar.agregarTerminal("]");
        grammar.agregarTerminal("sentence");
    
        // Producciones (simulando múltiples por no terminal)
        grammar.agregarProduccion("S", "S ^ P");
        grammar.agregarProduccion("S", "P");
        grammar.agregarProduccion("P", "P V Q");
        grammar.agregarProduccion("P", "Q");
        grammar.agregarProduccion("Q", "[ S ]");
        grammar.agregarProduccion("Q", "sentence");
    
        firstpos calc = new firstpos(grammar);
        calc.calcularFirstPos();
    }
    
}

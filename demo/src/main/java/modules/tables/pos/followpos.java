package modules.tables.pos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Grammar;

public class followpos {
    private Grammar grammar;
    private Map<String, Set<String>> firstMap = new HashMap<>();
    private Map<String, Set<String>> followMap = new HashMap<>();

    public followpos(Grammar grammar, Map<String, Set<String>> firstMap) {
        this.grammar = grammar;
        this.firstMap = firstMap;
        followMap = new HashMap<>();

        // Incializar sets vacios en el mapa follow
        for (String no_terminal_cabeza : grammar.getProductions().keySet()) {
            followMap.put(no_terminal_cabeza, new HashSet<>());
        }
    }

    public Map<String, Set<String>> getFollowPos(String simbolo_inicial) {
        Map<String, List<String>> todas_producciones = grammar.getProductions();

        for (String no_terminal_cabeza : todas_producciones.keySet()) {
            List<String> array_producciones_unKey = todas_producciones.get(no_terminal_cabeza);
            if (no_terminal_cabeza.equals(simbolo_inicial)) {
                // aplica regla 1
                followMap.get(no_terminal_cabeza).add("$");
            }
            for (String produccion_actual : array_producciones_unKey) { // Revisar todas las producciones para un NT
                if (produccion_actual == null) {
                    break;// Saltar transiciones epsilon
                }

                // Separar elementos en la producción
                String[] elementos_resultado = produccion_actual.trim().split("\\s+");

                for (int i = 0; i < elementos_resultado.length; i++) { // Iterar elementos en la procucción
                    String actual_element_str = elementos_resultado[i]; // Elemento como string

                    if (grammar.getNoTerminales().contains(actual_element_str)) { // Si elemento es un no terminal
                        Set<String> rules_result = check_rules(no_terminal_cabeza, i, elementos_resultado);
                        followMap.get(actual_element_str).addAll(rules_result); // añadir resultado al set en el mapa
                    }
                }

            }
        }

        // Buscar centinelas, ver si un follow depende de otro
        for (String no_terminal_cabeza : followMap.keySet()) {
            Set<String> thisFollowSet = followMap.get(no_terminal_cabeza); // Follow actual
            Set<String> newFollowSet = new HashSet<>(); // Follow a modificar
            for (String str_follow_element : thisFollowSet) {
                if (grammar.getNoTerminales().contains(str_follow_element)) { // Si elemento es un no terminal
                    // Copiar elementos 'follow' de otro key al key actual
                    newFollowSet.addAll(followMap.get(str_follow_element));
                } else {
                    newFollowSet.add(str_follow_element); // Copiar los elementos terminales
                }
            }
            followMap.put(no_terminal_cabeza, newFollowSet); // Reemplazar el conjunto actual con el nuevo conjunto
        }

        return followMap;
    }

    /**
     * Esta función inspecciona las reglas que pueden aplicarse a una producción,
     * dado un 'A'
     * 
     * @param B          'cabeza' de la produccion
     * @param A_position posición del elemento 'A' para la regla
     * @param produccion todos los elementos en la produccion
     * @return set con elementos a añadir al set de follow
     */
    private Set<String> check_rules(String B, int A_position, String[] produccion) {
        Set<String> result = new HashSet<>();
        if (produccion[A_position].equals(B)) {
            return result;
        }

        if (A_position == produccion.length - 1) {
            // beta = epsilon
            // aplico regla 3 (B -> ɑA)
            result.add(B); // B como centinela, debe usarse follow(B) al terminar
            return result;
        }

        // aplico B -> ɑAß
        String[] beta = Arrays.copyOfRange(produccion, A_position + 1, produccion.length);
        String first_beta = beta[0];
        if (grammar.getNoTerminales().contains(first_beta)) {

            // aplico regla 3 donde ß -> epsilon
            // considera que epsilon es guardado como un elemento nulo
            if (grammar.getProductions().get(first_beta).contains(null)) {
                result.add(B); // B como centinela, debe usarse follow(B) al terminar
                return result;
            }

            // aplico regla 2
            return firstMap.get(first_beta);
        }

        // aplico regla 2
        // beta no es un no terminal
        result.add(first_beta);
        return result;
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
        System.out.println("\nResultado FIRST:");
        for (Map.Entry<String, Set<String>> entry : tablaFirst.entrySet()) {
            System.out.println("first(" + entry.getKey() + ") = " + entry.getValue());
        }

        followpos follow_calc = new followpos(grammar, tablaFirst);
        Map<String, Set<String>> tablaFollow = follow_calc.getFollowPos("S");
        System.out.println("\nResultado FOLLOW:");
        for (Map.Entry<String, Set<String>> entry : tablaFollow.entrySet()) {
            System.out.println("Follow(" + entry.getKey() + ") = " + entry.getValue());
        }
    }
}
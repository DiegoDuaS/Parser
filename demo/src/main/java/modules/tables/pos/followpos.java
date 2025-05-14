package modules.tables.pos;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Grammar;

/**
 * Clase para calcular los conjuntos FOLLOW de una gramática libre de contexto.
 * 
 * Se basa en la gramática proporcionada y los conjuntos FIRST para
 * construir los conjuntos FOLLOW de cada no terminal, siguiendo las reglas
 * clásicas
 * de análisis sintáctico.
 * 
 * Reglas principales aplicadas:
 * - Regla 1: El símbolo inicial incluye '$' en su FOLLOW.
 * - Regla 2: Si un no terminal A es seguido por una cadena β, se agregan los
 * primeros símbolos de FIRST(β) a FOLLOW(A).
 * - Regla 3: Si β puede derivar a ε, se agrega FOLLOW del no terminal que
 * contiene a A.
 * 
 * Uso principal:
 * 1. Crear una instancia pasando una gramática y sus conjuntos FIRST.
 * 2. Llamar a `getFollowPos(simbolo_inicial)` para obtener la tabla de FOLLOW.
 * 
 * Nota: Esta implementación maneja producciones múltiples y símbolos de epsilon
 * como `null`.
 */
public class followpos {
    private Grammar grammar;
    private Map<String, Set<String>> firstMap;
    private Map<String, Set<String>> followMap = new HashMap<>();

    /**
     * Constructor de la clase followpos.
     * 
     * @param grammar  Objeto Grammar que contiene los no terminales, terminales y
     *                 producciones.
     * @param firstMap Mapa de conjuntos FIRST de cada no terminal.
     */
    public followpos(Grammar grammar, Map<String, Set<String>> firstMap) {
        this.grammar = grammar;
        this.firstMap = firstMap;

        // Incializar sets vacios en el mapa follow
        for (String no_terminal_cabeza : grammar.getProductions().keySet()) {
            followMap.put(no_terminal_cabeza, new HashSet<>());
        }
    }

    /**
     * Calcula y retorna los conjuntos FOLLOW para todos los no terminales de la
     * gramática.
     * 
     * @return Mapa que asocia cada no terminal con su conjunto FOLLOW
     *         correspondiente.
     */
    public Map<String, Set<String>> getFollowPos() {
        String simbolo_inicial = grammar.getInitialSimbol();
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

        int clean_sets = 0; // Cantidad de sets guardados sin epsilon ni no terminales
        int totalNoTerminales = followMap.keySet().size();

        while (clean_sets != totalNoTerminales) {
            clean_sets = 0;
            // Buscar centinelas, ver si un follow depende de otro
            for (String no_terminal_cabeza : followMap.keySet()) {
                Set<String> thisFollowSet = followMap.get(no_terminal_cabeza); // Follow actual
                Set<String> newFollowSet = new HashSet<>(); // Follow a modificar

                for (String str_follow_element : thisFollowSet) {
                    if (grammar.getNoTerminales().contains(str_follow_element)) { // Si elemento es un no terminal
                        // Copiar elementos 'follow' de otro key al key actual
                        newFollowSet.addAll(followMap.get(str_follow_element));
                    } else if (str_follow_element != null) {
                        newFollowSet.add(str_follow_element); // Copiar los elementos terminales
                    } // Omitir los nulos
                }
                followMap.put(no_terminal_cabeza, newFollowSet); // Reemplazar el conjunto actual con el nuevo conjunto

                if (!grammar.getNoTerminales().stream().anyMatch(newFollowSet::contains)) {
                    clean_sets++;
                }
            }

        }

        return followMap;
    }

    /**
     * Verifica y aplica las reglas para determinar qué elementos deben añadirse al
     * FOLLOW
     * de un no terminal dado, basado en su posición dentro de una producción.
     * 
     * @param B          No terminal "cabeza" de la producción actual.
     * @param A_position Índice donde se encuentra el no terminal A en la
     *                   producción.
     * @param produccion Arreglo que contiene los elementos (terminales y no
     *                   terminales) de la producción.
     * @return Un conjunto de strings que deben ser añadidos al FOLLOW del no
     *         terminal analizado.
     */
    private Set<String> check_rules(String B, int A_position, String[] produccion) {
        Set<String> result = new HashSet<>();

        if (A_position == produccion.length - 1) {
            // beta = epsilon
            if (produccion[A_position].equals(B)) { // A = B
                return result;
            }

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
            }

            // aplico regla 2
            result.addAll(firstMap.get(first_beta));
            return result;
        }

        // aplico regla 2
        // beta no es un no terminal
        result.add(first_beta);
        return result;
    }

    public static void main(String[] args) {
        Grammar grammar = new Grammar("S");

        grammar.agregarNoTerminal("E");
        grammar.agregarNoTerminal("E'");
        grammar.agregarNoTerminal("T");
        grammar.agregarNoTerminal("T'");
        grammar.agregarNoTerminal("F");

        grammar.agregarTerminal("+");
        grammar.agregarTerminal("(");
        grammar.agregarTerminal(")");
        grammar.agregarTerminal("*");
        grammar.agregarTerminal("id");

        grammar.agregarProduccion("E", "T E'");
        grammar.agregarProduccion("E'", "+ T E'");
        grammar.agregarProduccion("E'", null);
        grammar.agregarProduccion("T", "F T'");
        grammar.agregarProduccion("T'", "* F T'");
        grammar.agregarProduccion("T'", null);
        grammar.agregarProduccion("F", "( E )");
        grammar.agregarProduccion("F", "id");

        firstpos calc = new firstpos(grammar);
        Map<String, Set<String>> tablaFirst = calc.calcularFirstPos();
        System.out.println("\nResultado FIRST:");
        for (Map.Entry<String, Set<String>> entry : tablaFirst.entrySet()) {
            System.out.println("first(" + entry.getKey() + ") = " + entry.getValue());
        }

        followpos follow_calc = new followpos(grammar, tablaFirst);
        Map<String, Set<String>> tablaFollow = follow_calc.getFollowPos();
        System.out.println("\nResultado FOLLOW:");
        for (Map.Entry<String, Set<String>> entry : tablaFollow.entrySet()) {
            System.out.println("Follow(" + entry.getKey() + ") = " + entry.getValue());
        }
    }
}
package modules.tables.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Grammar;

public class followpos {
    private Grammar grammar;
    private Map<String, Set<String>> firstMap = new HashMap<>();
    private Map<String, Set<String>> followMap = new HashMap<>();

    public void getFollowPos() {
        Map<String, List<String>> todas_producciones = grammar.getProductions();

        for (String no_terminal_cabeza : todas_producciones.keySet()) {
            List<String> array_producciones_unKey = todas_producciones.get(no_terminal_cabeza);
            String simbolo_inicial = ""; // PENDIENTE
            if (no_terminal_cabeza.equals(simbolo_inicial)) {
                // aplica regla 1
                followMap.get(no_terminal_cabeza).add("$");
            }
            for (String produccion_actual : array_producciones_unKey) { // Q -> e^SP
                String alpha;
                List<Character> possible_A = new ArrayList<>();
                String beta;
                String B = no_terminal_cabeza;

                char[] elementos_resultado = produccion_actual.toCharArray();
                for (char actual_element : elementos_resultado) {
                    if (grammar.getNoTerminales().contains(actual_element)) {
                        possible_A.add(actual_element);
                    }
                }
            }
        }
    }
}

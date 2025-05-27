package modules.tables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.models.AFD;

import models.Grammar;

public class parsingTable {
    private AFD associatedAfd;
    private Grammar originalGrammar;
    private Map<String, Set<String>> followMap;
    private HashMap<String, String> parsingTable; // ESTADO, SIMBOLO = accion/movimiento

    public parsingTable(AFD Afd, Grammar grammar) {
        associatedAfd = Afd;
        originalGrammar = grammar;
        followMap = new HashMap<>();
        parsingTable = new HashMap<>();
    }

    // GoTo y Shifts dependen directamente del AFD
    private void saveGoTo() {
        Map<String, List<String>> transitions = associatedAfd.getTransitions_table();
        for (String transition_head : transitions.keySet()) {
            // Solo consideramos los terminales
            if (originalGrammar.getTerminales().contains(transition_head)) {
                // SHIFT
            } else if (originalGrammar.getNoTerminales().contains(transition_head)) {
                // GO TO
            }

        }
    }

    private void saveShifts() {

    }

    // Depende de los resultados de FOLLOW y la gramática original
    private void saveReduce() {
        // Asociar estados de aceptación a gramática
    }

}

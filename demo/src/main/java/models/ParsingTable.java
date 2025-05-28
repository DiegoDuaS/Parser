package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParsingTable {
    private AFD associatedAfd;
    private Grammar originalGrammar;
    private Map<String, Map<String, String>> actionTable; // estado, <simbolo, accion>
    private Map<String, Map<String, String>> goToTable; // estado, <simbolo, accion>
    private Map<String, ReduceEntry> reduceDictionary = new HashMap<>(); // nombre (R#), <estado, produccion>

    public ParsingTable(AFD associatedAfd, Grammar originalGrammar) {
        this.associatedAfd = associatedAfd;
        this.originalGrammar = originalGrammar;

        this.actionTable = new HashMap<>();
        this.goToTable = new HashMap<>();

        // Inicializar filas por cada estado del AFD
        for (String stateId : associatedAfd.getEstados().keySet()) {
            actionTable.put(stateId, new HashMap<>());
            goToTable.put(stateId, new HashMap<>());
        }
    }

    public Map<String, Map<String, String>> getActionTable() {
        return actionTable;
    }

    public Map<String, Map<String, String>> getGoToTable() {
        return goToTable;
    }

    public AFD getAssociatedAfd() {
        return associatedAfd;
    }

    public Grammar getOriginalGrammar() {
        return originalGrammar;
    }

    public Map<String, ReduceEntry> getReduceDictionary() {
        return reduceDictionary;
    }

    public void agregarReduceValue(String reduceName, ReduceEntry produccion) {
        this.reduceDictionary.put(reduceName, produccion);
    }

    public void agregarAction(String state, String symbol, String action) {
        this.actionTable
                .computeIfAbsent(state, k -> new HashMap<>())
                .put(symbol, action);
    }

    public void agregarGoTo(String state, String nonTerminal, String nextState) {
        this.goToTable
                .computeIfAbsent(state, k -> new HashMap<>())
                .put(nonTerminal, nextState);
    }

    public void printParsingTables(String sentinel) {
        // Terminales y no terminales
        List<String> terminales = getOriginalGrammar().getTerminales();
        terminales.add(sentinel);
        List<String> noTerminales = getOriginalGrammar().getNoTerminales();

        // ACTION TABLE
        System.out.println("\n=== ACTION TABLE ===");

        // Encabezado
        System.out.printf("%-10s", "Estado");
        for (String symbol : terminales) {
            System.out.printf(" | %-10s", symbol);
        }
        System.out.println();
        System.out.println("-----------" + "-".repeat(terminales.size() * 13));

        // Filas por estado
        for (String estado : getActionTable().keySet()) {
            System.out.printf("%-10s", estado);
            for (String symbol : terminales) {
                String action = getActionTable().get(estado).getOrDefault(symbol, "");
                System.out.printf(" | %-10s", action);
            }
            System.out.println();
        }

        // GO-TO TABLE
        System.out.println("\n=== GO-TO TABLE ===");

        // Encabezado
        System.out.printf("%-10s", "Estado");
        for (String symbol : noTerminales) {
            System.out.printf(" | %-10s", symbol);
        }
        System.out.println();
        System.out.println("-----------" + "-".repeat(noTerminales.size() * 13));

        // Filas por estado
        for (String estado : getGoToTable().keySet()) {
            System.out.printf("%-10s", estado);
            for (String symbol : noTerminales) {
                String goTo = getGoToTable().get(estado).getOrDefault(symbol, "");
                System.out.printf(" | %-10s", goTo);
            }
            System.out.println();
        }
    }

    public void printReduceDictionary() {
        System.out.println("\n=== REDUCE ENTRIES ===");
        for (Map.Entry<String, ReduceEntry> entry : reduceDictionary.entrySet()) {
            String reduceName = entry.getKey();
            ReduceEntry info = entry.getValue();

            System.out.printf(
                    "%s | %s -> %s | Estado: %s%n",
                    reduceName,
                    info.getProduction_head(),
                    info.getProduction_value(),
                    info.getState());
        }
    }

}

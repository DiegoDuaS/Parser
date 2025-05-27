package models;

import java.util.HashMap;
import java.util.Map;

public class ParsingTable {
    private AFD associatedAfd;
    private Grammar originalGrammar;
    private Map<String, Map<String, String>> actionTable;
    private Map<String, Map<String, String>> goToTable;

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

    public void setActionTable(Map<String, Map<String, String>> actionTable) {
        this.actionTable = actionTable;
    }

    public void setGoToTable(Map<String, Map<String, String>> goToTable) {
        this.goToTable = goToTable;
    }



}

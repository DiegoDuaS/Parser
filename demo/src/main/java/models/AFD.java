package models;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AFD implements Serializable {
    private static final long serialVersionUID = 1L;

    private LinkedHashMap<String, EstadoAFD> estados;
    private Map<String, Map<String, String>> transitionsTable;
    private String initialState;
    private List<String> acceptanceStates;

    public AFD(LinkedHashMap<String, EstadoAFD> estados, Map<String, Map<String, String>> transitionsTable,
               String initialState, List<String> acceptanceStates) {
        this.estados = estados;
        this.transitionsTable = transitionsTable;
        this.initialState = initialState;
        this.acceptanceStates = acceptanceStates;
    }

    // Getters y setters
    public LinkedHashMap<String, EstadoAFD> getEstados() { 
        return estados; 
    }
    public Map<String, Map<String, String>> getTransitionsTable() { 
        return transitionsTable; 
    }
    public String getInitialState() { 
        return initialState; 
    }
    public List<String> getAcceptanceStates() {
         return acceptanceStates; 
    }

    public void setEstados(LinkedHashMap<String, EstadoAFD> estados) {
        this.estados = estados;
    }

    public void setTransitionsTable(Map<String, Map<String, String>> transitionsTable) {
        this.transitionsTable = transitionsTable;
    }

    public void setInitialState(String initialState) {
        this.initialState = initialState;
    }

    public void setAcceptanceStates(List<String> acceptanceStates) {
        this.acceptanceStates = acceptanceStates;
    }
}

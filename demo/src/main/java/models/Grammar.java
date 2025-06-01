package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Grammar implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, List<String>> productions; // Producciones por no terminal
    private List<String> terminales; // Terminales
    private List<String> noTerminales; // No terminales
    private String initialSimbol;

    public Grammar(Map<String, List<String>> productions, List<String> terminales, List<String> noTerminales,
            String initialSimbol) {
        this.productions = productions;
        this.terminales = terminales;
        this.noTerminales = noTerminales;
        this.initialSimbol = initialSimbol;
    }

    public Grammar(String initialSimbol) {
        this.productions = new LinkedHashMap<>();
        this.terminales = new ArrayList<>();
        this.noTerminales = new ArrayList<>();
        this.initialSimbol = initialSimbol;
    }

    // Método para agregar una producción
    public void agregarProduccion(String noTerminal, String produccion) {
        if (!productions.containsKey(noTerminal)) {
            productions.put(noTerminal, new ArrayList<>());
        }
        productions.get(noTerminal).add(produccion);
    }

    // Métodos para agregar terminales y no terminales
    public void agregarTerminal(String simbolo) {
        terminales.add(simbolo);
    }

    public void agregarNoTerminal(String simbolo) {
        noTerminales.add(simbolo);
    }

    // Getters para obtener las producciones, terminales y no terminales
    public Map<String, List<String>> getProductions() {
        return productions;
    }

    public List<String> getTerminales() {
        return terminales;
    }

    public List<String> getNoTerminales() {
        return noTerminales;
    }

    public String getInitialSimbol() {
        return initialSimbol;
    }

    public void setInitialSimbol(String initialSimbol) {
        this.initialSimbol = initialSimbol;
    }
}

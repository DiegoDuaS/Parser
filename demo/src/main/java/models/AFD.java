package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AFD implements Serializable {
    private static final long serialVersionUID = 1L; // Asegura la compatibilidad de la serialización

    private HashMap<String, List<String>> transitions_table;
    private List<String> states;
    private List<String> alphabet;
    private String initial_state;
    private List<String> acceptance_states;

    public AFD(HashMap<String, List<String>> transitions_table, List<String> states, List<String> alphabet,
     String initial_state, List<String> acceptance_states) {
        this.transitions_table = transitions_table;
        this.states = states;
        this.alphabet = alphabet;
        this.initial_state = initial_state;
        this.acceptance_states = acceptance_states;
    }

    public HashMap<String, List<String>> getTransitions_table() {
        return transitions_table;
    }

    public void setTransitions_table(HashMap<String, List<String>> transitions_table) {
        this.transitions_table = transitions_table;
    }

    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(List<String> alphabet) {
        this.alphabet = alphabet;
    }

    public String getInitial_state() {
        return initial_state;
    }

    public void setInitial_state(String initial_state) {
        this.initial_state = initial_state;
    }

    public List<String> getAcceptance_states() {
        return acceptance_states;
    }

    public void setAcceptance_states(List<String> acceptance_states) {
        this.acceptance_states = acceptance_states;
    }

    /*
     * q --> state
     * a --> alphabet element
     * d --> list of transitions
     * 
     * @return transition's value
     */
    private String transition(String q, String symbol) {
        String transited_state = "";
        List<String> transitions = transitions_table.get(q);
        for (int i = 0; i < alphabet.size(); i++) {
            if (symbol.equals(String.valueOf(alphabet.get(i)))) {
                transited_state = transitions.get(i);
                break;
            }
        }
        if (symbol != "\0" &&
                transited_state == null &&
                alphabet.contains("\0")) {
            int epsilonPosition = alphabet.indexOf("\0");
            transited_state = "\0" + transitions.get(epsilonPosition);
        }
        return transited_state;
    }

    /*
     * q --> state
     * F --> Acceptance state
     */
    public Boolean accepted(String q, List<String> F) {
        return F.contains(q);
    }

    /*
     * q --> state
     * w --> string
     * 
     * @return secuence of states
     */
    public ArrayList<ArrayList<String>> derivation(String q, String w) {
        String state = q;
        ArrayList<ArrayList<String>> transitions = new ArrayList<>();

        for (int i = 0; i < w.length(); i++) {
            ArrayList<String> actualT = new ArrayList<>(); // Guarda la transición actual
            actualT.add(state);
            actualT.add(String.valueOf(w.charAt(i)));

            if (String.valueOf(w.charAt(i)).equals("#") && i == w.length() - 1) {
                // Antes del centinela verificar si hay una transición epsilon a considerar
                if (alphabet.contains("\0")) {
                    state = transition(state, "\0");
                    if (state != null) {
                        actualT.remove(1);
                        actualT.add(String.valueOf("\0"));
                        actualT.add(state);
                    }
                }
                // Detenerse al llegar al centinela
                actualT.add(actualT.get(0)); // Permanece en el último estado añadido
                transitions.add(actualT);
                break;
            }

            state = transition(state, w.charAt(i) + "");

            // Cuando se tomó en cuenta una transición con epsilon
            if (state.startsWith("\0")) {
                // Añadir la transición epsilon
                actualT.remove(1);
                actualT.add(String.valueOf("\0"));
                actualT.add(state);
                transitions.add(actualT);

                // Repetir proceso con el símbolo actual
                state = state.replace("\0", "");
                actualT.clear();
                actualT.add(state);
                actualT.add(String.valueOf(w.charAt(i)));
                state = transition(state, w.charAt(i) + "");
            }

            actualT.add(state);

            transitions.add(actualT);
        }

        return transitions;
    }

    public void printAFD() {
        System.out.println("\n Alfabeto: " + alphabet.toString());

        System.out.println("\n Estado inicial: " + initial_state);

        System.out.println("\n Estados de aceptación: " + acceptance_states.toString());

        System.out.println("\n Todos los estados: " + states.toString());

        System.out.println("\n TRANSICIONES");
        for (String ns : transitions_table.keySet()) {
            System.out.println(ns + "\t" + transitions_table.get(ns));
        }

    }

    public static void main(String[] args) {

        // Definir estados
        List<String> states = Arrays.asList("q0", "q1", "q2", "q3");

        // Definir alfabeto
        List<String> alphabet = Arrays.asList("0", "1");

        // Definir estado inicial
        String initial_state = "q0";

        // Definir estados de aceptación
        List<String> acceptance_states = Arrays.asList("q1", "q3");

        // Definir transiciones
        HashMap<String, List<String>> transitions = new HashMap<>();
        transitions.put("q0", Arrays.asList("q1", "q2"));
        transitions.put("q1", Arrays.asList("q0", "q3"));
        transitions.put("q2", Arrays.asList("q3", "q0"));
        transitions.put("q3", Arrays.asList("q2", "q1"));

        AFD afd = new AFD(transitions, states, alphabet, initial_state, acceptance_states);
        afd.printAFD();
    }
}

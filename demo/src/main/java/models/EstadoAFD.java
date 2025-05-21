package models;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class EstadoAFD {
    private String id;
    private List<GrammarExtended.ProductionWithPointer> items;
    private String initial_state;
    private List<String> acceptance_states;
    private HashMap<String, List<String>> transitions_table;

    public EstadoAFD(String id) {
        this.id = id;
        this.items = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public List<GrammarExtended.ProductionWithPointer> getItems() {
        return items;
    }

    public void agregarItem(GrammarExtended.ProductionWithPointer item) {
        items.add(item);
    }

    public boolean contieneItem(GrammarExtended.ProductionWithPointer item) {
        for (GrammarExtended.ProductionWithPointer prod : items) {
            if (prod.getSymbols().equals(item.getSymbols()) && prod.getPointer() == item.getPointer()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(id + ":\n");
        for (GrammarExtended.ProductionWithPointer item : items) {
            sb.append("  ").append(item).append("\n");
        }
        return sb.toString();
    }

    public void setItems(LinkedList<GrammarExtended.ProductionWithPointer> items){
        this.items = items;
    }

    public String getInitial_state() {
        return initial_state;
    }

    public void setInitial_state(String id){
        this.initial_state = id;
    }

    public List<String> getAcceptance_states() {
        return acceptance_states;
    }

    public void setAccpetance_state(String id){
        this.acceptance_states.add(id);
    }

    public void setTransitions_table(HashMap<String, List<String>> transitions_table) {
        this.transitions_table = transitions_table;
    }

    public HashMap<String, List<String>> getTransitions_table() {
        return transitions_table;
    }
}


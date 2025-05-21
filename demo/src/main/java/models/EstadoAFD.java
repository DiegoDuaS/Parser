package models;
import java.util.LinkedList;
import java.util.List;

public class EstadoAFD {
    private String id;
    private List<GrammarExtended.ProductionWithPointer> items;

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

}


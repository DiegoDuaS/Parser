package models;
import java.util.ArrayList;
import java.util.List;

public class EstadoAFD {
    private String nombre;
    private List<GrammarExtended.ProductionWithPointer> items;

    public EstadoAFD(String nombre) {
        this.nombre = nombre;
        this.items = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
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
        StringBuilder sb = new StringBuilder(nombre + ":\n");
        for (GrammarExtended.ProductionWithPointer item : items) {
            sb.append("  ").append(item).append("\n");
        }
        return sb.toString();
    }

    public Object getTransiciones() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}


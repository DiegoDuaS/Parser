package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GrammarExtended {

    private String initialSymbol;
    private Map<String, List<ProductionWithPointer>> productions = new LinkedHashMap<>();
    private List<String> terminales; // Terminales
    private List<String> noTerminales; // No terminales


    public GrammarExtended(String initialSymbol) {
        this.initialSymbol = initialSymbol;
        this.productions = new LinkedHashMap<>();
        this.terminales = new ArrayList<>();
        this.noTerminales = new ArrayList<>();
    }

    public void agregarProduccion(String noTerminal, List<String> produccion, int pointer) {
        ProductionWithPointer prod = new ProductionWithPointer(produccion, pointer);
        productions.computeIfAbsent(noTerminal, k -> new ArrayList<>()).add(prod);
    }

    public String getInitialSymbol() {
        return initialSymbol;
    }

    public void setInitialSymbol(String initialSymbol) {
        this.initialSymbol = initialSymbol;
    }

    public Map<String, List<ProductionWithPointer>> getProductions() {
        return productions;
    }

    public void imprimirProducciones() {
        for (Map.Entry<String, List<ProductionWithPointer>> entry : productions.entrySet()) {
            String noTerminal = entry.getKey();
            for (ProductionWithPointer prod : entry.getValue()) {
                System.out.println(noTerminal + " -> " + prod);
            }
        }
    }

    public List<String> getTerminales() {
        return terminales;
    }

    public void setTerminales(List<String> terminales) {
        this.terminales = terminales;
    }

    public List<String> getNoTerminales() {
        return noTerminales;
    }

    public void setNoTerminales(List<String> noTerminales) {
        this.noTerminales = noTerminales;
    }

    public static class ProductionWithPointer {
        private List<String> symbols;
        private int pointer;

        public ProductionWithPointer(List<String> symbols, int pointer) {
            this.symbols = symbols;
            this.pointer = pointer;
        }

        public List<String> getSymbols() {
            return symbols;
        }

        public int getPointer() {
            return pointer;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < symbols.size(); i++) {
                if (i == pointer) sb.append(".");
                sb.append(symbols.get(i)).append(" ");
            }
            if (pointer == symbols.size()) sb.append(".");
            return sb.toString().trim();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProductionWithPointer that = (ProductionWithPointer) o;
            return pointer == that.pointer &&
                Objects.equals(symbols, that.symbols);
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbols, pointer);
        }
        
    }
}

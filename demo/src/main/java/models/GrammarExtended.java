package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GrammarExtended {

    private String initialSymbol;
    // Producciones: NoTerminal -> Lista de (producción, puntero)
    private Map<String, List<ProductionWithPointer>> productions = new LinkedHashMap<>();


    public GrammarExtended(String initialSymbol) {
        this.initialSymbol = initialSymbol;
        this.productions = new LinkedHashMap<>();
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
    }
}

package modules.tables;

import java.util.List;
import java.util.Map;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.ParsingTable;
import models.GrammarExtended.ProductionWithPointer;
import modules.automaton.automatom;
import modules.automaton.extension;

public class generateParseTable {
    ParsingTable parseTable;

    public generateParseTable(ParsingTable table) {
        parseTable = table;

        // Registrar Goto y Switch
        travelAFD();

        // Registrar Reduce
    }

    private void travelAFD() {
        Map<String, Map<String, String>> transitions = parseTable.getAssociatedAfd().getTransitionsTable();
        List<String> terminales = parseTable.getOriginalGrammar().getTerminales();
        List<String> noTerminales = parseTable.getOriginalGrammar().getNoTerminales();

        for (Map.Entry<String, Map<String, String>> entry : transitions.entrySet()) {
            String from = entry.getKey();
            for (Map.Entry<String, String> trans : entry.getValue().entrySet()) {
                String symbol = trans.getKey();
                if (terminales.contains(symbol)) {
                    // SHIFT
                    parseTable.agregarAction(from, symbol, "S" + trans.getValue());
                } else if (noTerminales.contains(symbol)) {
                    // GO TO
                    parseTable.agregarGoTo(from, symbol, trans.getValue());
                }
            }

        }
    }

    public static void main(String[] args) {
        // Generar gramática base
        Grammar g = new Grammar("S");
        g.agregarNoTerminal("S");
        g.agregarNoTerminal("P");
        g.agregarNoTerminal("Q");

        g.agregarTerminal("^");
        g.agregarTerminal("V");
        g.agregarTerminal("[");
        g.agregarTerminal("]");
        g.agregarTerminal("sentence");

        g.agregarProduccion("S", "S ^ P");
        g.agregarProduccion("S", "P");
        g.agregarProduccion("P", "P V Q");
        g.agregarProduccion("P", "Q");
        g.agregarProduccion("Q", "[ S ]");
        g.agregarProduccion("Q", "sentence");

        // Extender la gramática
        GrammarExtended extendida = extension.extenderGramatica(g);

        // Crear el estado inicial del AFD
        EstadoAFD estado0 = automatom.crearEstadoInicial(extendida);
        System.out.println("\nEstado inicial:");
        System.out.println(estado0.getId());
        for (ProductionWithPointer item : estado0.getItems()) {
            System.out.println("  " + item);
        }

        // Generar AFD
        AFD afd = automatom.generarAFD(extendida, estado0);

        // Crear objeto para tabla de parseo
        ParsingTable parseTable = new ParsingTable(afd, g);

        new generateParseTable(parseTable);

        // IMPRIMIR RESULTADOS
        // action table
        System.out.println("\n=== ACTION TABLE ===");
        for (Map.Entry<String, Map<String, String>> entry : parseTable.getActionTable().entrySet()) {
            String from = entry.getKey();
            for (Map.Entry<String, String> trans : entry.getValue().entrySet()) {
                System.out.println(from + ", " + trans.getKey() + " = " + trans.getValue());
            }
        }

        // go to table
        System.out.println("\n=== GO-TO TABLE ===");
        for (Map.Entry<String, Map<String, String>> entry : parseTable.getGoToTable().entrySet()) {
            String from = entry.getKey();
            for (Map.Entry<String, String> trans : entry.getValue().entrySet()) {
                System.out.println(from + ", " + trans.getKey() + " = " + trans.getValue());
            }
        }
    }
}

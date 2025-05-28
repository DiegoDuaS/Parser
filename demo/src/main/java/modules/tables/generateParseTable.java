package modules.tables;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.ParsingTable;
import modules.automaton.automatom;
import modules.automaton.extension;
import modules.tables.pos.firstpos;
import modules.tables.pos.followpos;

public class generateParseTable {

    public static void travelAFD(ParsingTable parseTable) {
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

    public static void reduceTable(ParsingTable parseTable, Map<String, Set<String>> tablaFollow){
        Map<String, Map<Map<String, List<String>>, String>> reduceRelations = new HashMap<>();
        List<String> acceptanceStates = parseTable.getAssociatedAfd().getAcceptanceStates();
        LinkedHashMap<String, EstadoAFD> estados = parseTable.getAssociatedAfd().getEstados();
        Map<String, List<String>> produccionesOG = parseTable.getOriginalGrammar().getProductions();
        acceptanceStates.remove(0);

        int reductionCounter = 1;
        for(String id: acceptanceStates){
            for(EstadoAFD estado : estados.values()){
                for (GrammarExtended.ProductionWithPointer production : estado.getItems()) {
                    if (production.getPointer() == production.getSymbols().size()) {
                        System.out.println(id + "-" + production.getSymbols() + "-" + "R" + reductionCounter);
                        reductionCounter++;
                        break;
                    }
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
        
        // Generar AFD
        AFD afd = automatom.generarAFD(extendida, estado0);

        // Crear objeto para tabla de parseo
        ParsingTable parseTable = new ParsingTable(afd, g);

        travelAFD(parseTable);

        firstpos first_calc = new firstpos(g);
        Map<String, Set<String>> tablaFirst = first_calc.calcularFirstPos();

        followpos follow_calc = new followpos(g, tablaFirst);
        Map<String, Set<String>> tablaFollow = follow_calc.getFollowPos();

        reduceTable(parseTable, tablaFollow);

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

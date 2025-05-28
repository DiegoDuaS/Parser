package modules.tables;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.ParsingTable;
import models.ReduceEntry;
import modules.automaton.automatom;
import modules.automaton.extension;
import modules.tables.pos.firstpos;
import modules.tables.pos.followpos;

public class generateParseTable {

    public generateParseTable(AFD relatedAfd, Grammar originalGrammar) {
        // Crear objeto para tabla de parseo
        ParsingTable parseTable = new ParsingTable(relatedAfd, originalGrammar);

        travelAFD(parseTable);

        firstpos first_calc = new firstpos(originalGrammar);
        Map<String, Set<String>> tablaFirst = first_calc.calcularFirstPos();

        followpos follow_calc = new followpos(originalGrammar, tablaFirst);
        Map<String, Set<String>> tablaFollow = follow_calc.getFollowPos();

        reduceTable(parseTable, tablaFollow);

        // IMPRIMIR RESULTADOS
        parseTable.printParsingTables(follow_calc.getSentinel());
        System.out.println();
        parseTable.printReduceDictionary();
    }

    /**
     * Recorre la tabla de transiciones del AFD asociado a la tabla de análisis LR
     * y registra las acciones correspondientes (SHIFT o GOTO) en la tabla de
     * parsing.
     *
     * Para cada transición:
     * - Si el símbolo es un terminal, se registra una acción SHIFT.
     * - Si el símbolo es un no terminal, se registra una transición GOTO.
     */
    public static void travelAFD(ParsingTable parseTable) {
        // Obtener la tabla de transiciones del AFD
        Map<String, Map<String, String>> transitions = parseTable.getAssociatedAfd().getTransitionsTable();

        // Obtener terminales y no terminales de la gramática original
        List<String> terminales = parseTable.getOriginalGrammar().getTerminales();
        List<String> noTerminales = parseTable.getOriginalGrammar().getNoTerminales();

        // Recorrer todos los estados y sus transiciones
        for (Map.Entry<String, Map<String, String>> entry : transitions.entrySet()) {
            String from = entry.getKey(); // Estado origen
            for (Map.Entry<String, String> trans : entry.getValue().entrySet()) {
                String symbol = trans.getKey(); // Símbolo de transición
                String to = trans.getValue(); // Estado destino

                if (terminales.contains(symbol)) {
                    // Si el símbolo es terminal, registrar acción SHIFT (desplazamiento)
                    parseTable.agregarAction(from, symbol, "S" + to);
                } else if (noTerminales.contains(symbol)) {
                    // Si el símbolo es no terminal, registrar transición GOTO
                    parseTable.agregarGoTo(from, symbol, to);
                }
            }
        }
    }

    /**
     * Llena la tabla de análisis LR con las acciones de tipo REDUCE, basadas en los
     * estados
     * de aceptación del AFD y las producciones completas.
     *
     * Para cada estado de aceptación, si contiene una producción con el puntero al
     * final (completa),
     * se crea una entrada REDUCE con la producción correspondiente.
     *
     * Luego, usando la tabla FOLLOW, se registran las acciones REDUCE en la tabla
     * de parsing.
     */
    public static void reduceTable(ParsingTable parseTable, Map<String, Set<String>> tablaFollow) {
        // Obtener los estados de aceptación del AFD asociado a la tabla
        List<String> acceptanceStates = parseTable.getAssociatedAfd().getAcceptanceStates();
        // Obtener todos los estados del AFD
        LinkedHashMap<String, EstadoAFD> estados = parseTable.getAssociatedAfd().getEstados();
        // Obtener todas las producciones de la gramática original
        Map<String, List<String>> produccionesOG = parseTable.getOriginalGrammar().getProductions();

        // Se remueve el primer estado de aceptación (posiblemente el estado inicial con
        // la producción aumentada)
        acceptanceStates.remove(0);

        int reductionCounter = 1; // Contador para generar nombres únicos de reducciones

        // Recorrer cada estado de aceptación
        for (String id : acceptanceStates) {
            EstadoAFD estado = estados.get(id);

            // Buscar producciones completas (el puntero está al final de la producción)
            for (GrammarExtended.ProductionWithPointer production : estado.getItems()) {
                if (production.getPointer() == production.getSymbols().size()) {
                    String reduceName = "R" + reductionCounter;
                    reductionCounter++;

                    // Convertir los símbolos de la producción en un string plano
                    String acceptanceProduction = "";
                    for (String string : production.getSymbols()) {
                        acceptanceProduction += string + " ";
                    }
                    acceptanceProduction = acceptanceProduction.trim();

                    // Buscar la producción original correspondiente (head -> body) en la gramática
                    for (Entry<String, List<String>> producciones_de_un_key : produccionesOG.entrySet()) {
                        for (String produccionKey : producciones_de_un_key.getValue()) {
                            if (acceptanceProduction.equals(produccionKey)) {
                                // Crear y guardar una entrada REDUCE con estado, cabeza y producción
                                ReduceEntry reduceResult = new ReduceEntry(id, producciones_de_un_key.getKey(),
                                        produccionKey);
                                parseTable.agregarReduceValue(reduceName, reduceResult);
                                break; // Solo se necesita una coincidencia
                            }
                        }
                    }

                    break; // Solo se procesa una producción completa por estado
                }
            }
        }

        // Recorrer las reducciones registradas para agregar acciones REDUCE en la tabla
        // LR
        for (Entry<String, ReduceEntry> reduce : parseTable.getReduceDictionary().entrySet()) {
            String reduceName = reduce.getKey(); // Ej: "R3"
            ReduceEntry productionInfo = reduce.getValue(); // Contiene estado, head y producción

            // Obtener los símbolos del FOLLOW del no terminal (head) de la producción
            Set<String> symbolsToSet = tablaFollow.get(productionInfo.getProduction_head());

            // Agregar una acción REDUCE para cada símbolo del FOLLOW
            for (String symbol : symbolsToSet) {
                parseTable.agregarAction(productionInfo.getState(), symbol, reduceName);
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

        new generateParseTable(afd, g);
    }
}

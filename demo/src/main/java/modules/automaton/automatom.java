package modules.automaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import models.AFD;
import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.GrammarExtended.ProductionWithPointer;
import static modules.automaton.extension.extenderGramatica;

public class automatom {

    public static EstadoAFD crearEstadoInicial(GrammarExtended gext) {
        EstadoAFD estado0 = new EstadoAFD("0");
        // Recorremos todas las producciones extendidas
        for (List<GrammarExtended.ProductionWithPointer> producciones : gext.getProductions().values()) {
            for (GrammarExtended.ProductionWithPointer prod : producciones) {
                estado0.agregarItem(prod);
            }
        }
        return estado0;
    }

     public static List<GrammarExtended.ProductionWithPointer> closure(String simbolo, GrammarExtended gext) {
        List<GrammarExtended.ProductionWithPointer> resultado = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Queue<String> porVisitar = new LinkedList<>();

        porVisitar.add(simbolo);
        visitados.add(simbolo);

        while (!porVisitar.isEmpty()) {
            String actual = porVisitar.poll();

            List<GrammarExtended.ProductionWithPointer> producciones = gext.getProductions().get(actual);
            if (producciones != null) {
                for (GrammarExtended.ProductionWithPointer prod : producciones) {
                    resultado.add(prod);

                    // Si el primer símbolo de la producción es un no terminal, agregarlo a visitar
                    if (!prod.getSymbols().isEmpty()) {
                        String primerSimbolo = prod.getSymbols().get(0);
                        if (gext.getProductions().containsKey(primerSimbolo) && !visitados.contains(primerSimbolo)) {
                            porVisitar.add(primerSimbolo);
                            visitados.add(primerSimbolo);
                        }
                    }
                }
            }
        }

        return resultado;
    }

    public static AFD generarAFD(GrammarExtended gext, EstadoAFD estadoInicial) {
        LinkedHashMap<String, EstadoAFD> estados = new LinkedHashMap<>();
        List<String> noTerminales = gext.getNoTerminales();
        int statecheck = 0;
        int statescreated = 0;
        Map<String, Map<String, String>> transitionsTable = new HashMap<>();
        List<String> acceptanceStates = new LinkedList<>();

        estados.put(estadoInicial.getId(), estadoInicial);

        while (statecheck <= statescreated) {
            EstadoAFD actual = estados.get(String.valueOf(statecheck));
            LinkedHashMap<String, List<GrammarExtended.ProductionWithPointer>> movimientos = new LinkedHashMap<>();

            for (GrammarExtended.ProductionWithPointer item : actual.getItems()) {
                if (item.getPointer() < item.getSymbols().size()) {
                    String simbolo = item.getSymbols().get(item.getPointer());
                    GrammarExtended.ProductionWithPointer nuevo = new GrammarExtended.ProductionWithPointer(item.getSymbols(), item.getPointer() + 1);
                    movimientos.computeIfAbsent(simbolo, k -> new ArrayList<>()).add(nuevo);
                } else {
                    if (!acceptanceStates.contains(actual.getId())) {
                        acceptanceStates.add(actual.getId());
                    }
                }
            }

            for (Map.Entry<String, List<GrammarExtended.ProductionWithPointer>> transicion : movimientos.entrySet()) {
                String simbolo = transicion.getKey();
                List<GrammarExtended.ProductionWithPointer> nuevosItems = transicion.getValue();
                boolean encontrado = false;
                String destinoId = null;
                List<GrammarExtended.ProductionWithPointer> closureItems = null;
                List<GrammarExtended.ProductionWithPointer> itemsCompletos = new ArrayList<>(nuevosItems);

                for (GrammarExtended.ProductionWithPointer item : nuevosItems) {
                    if (item.getPointer() < item.getSymbols().size()) {
                        String simboloActual = item.getSymbols().get(item.getPointer());
                        if (noTerminales.contains(simboloActual)) {
                            closureItems = closure(simboloActual, gext);
                        }
                    }
                }

                if (closureItems != null) {
                    itemsCompletos.addAll(closureItems);
                }

                for (EstadoAFD existente : estados.values()) {
                    Set<GrammarExtended.ProductionWithPointer> setExistente = new HashSet<>(existente.getItems());
                    Set<GrammarExtended.ProductionWithPointer> setNuevo = new HashSet<>(itemsCompletos);
                    if (setExistente.equals(setNuevo)) {
                        encontrado = true;
                        destinoId = existente.getId();
                        break;
                    }
                }

                if (!encontrado) {
                    statescreated++;
                    EstadoAFD nuevoEstado = new EstadoAFD(String.valueOf(statescreated));
                    for (GrammarExtended.ProductionWithPointer item : itemsCompletos) {
                        nuevoEstado.agregarItem(item);
                    }
                    estados.put(nuevoEstado.getId(), nuevoEstado);
                    destinoId = nuevoEstado.getId();
                }

                transitionsTable
                    .computeIfAbsent(actual.getId(), k -> new HashMap<>())
                    .put(simbolo, destinoId);
            }
            statecheck++;
        }

        return new AFD(estados, transitionsTable, estadoInicial.getId(), acceptanceStates);
    }


    public static void main(String[] args) {
        // 1. Crear gramática base
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

        // 2. Extender la gramática
        GrammarExtended extendida = extenderGramatica(g);

        // 3. Mostrar producciones extendidas
        System.out.println("Producciones extendidas:");
        for (Map.Entry<String, List<ProductionWithPointer>> entry : extendida.getProductions().entrySet()) {
            for (ProductionWithPointer prod : entry.getValue()) {
                System.out.println(entry.getKey() + " -> " + prod);
            }
        }

        // 4. Crear el estado inicial del AFD
        EstadoAFD estado0 = crearEstadoInicial(extendida);
        System.out.println("\nEstado inicial:");
        System.out.println(estado0.getId());
        for (ProductionWithPointer item : estado0.getItems()) {
            System.out.println("  " + item);
        }

        // 5. Generar todos los estados del AFD
        AFD afd = generarAFD(extendida, estado0);

        System.out.println("\n=== ESTADOS DEL AFD ===");
        for (Map.Entry<String, EstadoAFD> entry : afd.getEstados().entrySet()) {
            System.out.println("Estado ID: " + entry.getKey());
            for (ProductionWithPointer item : entry.getValue().getItems()) {
                System.out.println("  " + item);
            }
        }

        System.out.println("\n=== TABLA DE TRANSICIONES ===");
        for (Map.Entry<String, Map<String, String>> entry : afd.getTransitionsTable().entrySet()) {
            String from = entry.getKey();
            for (Map.Entry<String, String> trans : entry.getValue().entrySet()) {
                System.out.println("δ(" + from + ", " + trans.getKey() + ") = " + trans.getValue());
            }
        }

        System.out.println("\n=== ESTADOS DE ACEPTACIÓN ===");
        for (String state : afd.getAcceptanceStates()) {
            System.out.println("Estado: " + state);
        }

        }
    
    
}

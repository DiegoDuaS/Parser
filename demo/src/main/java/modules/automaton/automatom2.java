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

public class automatom2 {

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

    public static Map<String, List<GrammarExtended.ProductionWithPointer>> construirClosures(GrammarExtended gext) {
        Map<String, List<GrammarExtended.ProductionWithPointer>> closureMap = new HashMap<>();
        for (String noTerminal : gext.getNoTerminales()) {
            closureMap.put(noTerminal, closure(noTerminal, gext));
        }
        return closureMap;
    }

    public static Map<String, EstadoAFD> generarEstados(GrammarExtended gext, EstadoAFD estadoInicial){
        LinkedHashMap<String, EstadoAFD> estados = new LinkedHashMap<>();
        List<String> noTerminales = gext.getNoTerminales();
        int statecheck = 0;
        int statescreated = 0; 
        HashMap<String, List<String>> transitionsTable;
        List<String> states = new LinkedList<>(); 
        List<String> acceptance_states = new LinkedList<>(); 

        estados.put(estadoInicial.getId(), estadoInicial);

        //Se corre hasta que se checqueen todos los estados que se van creando dinamicamente
        while(statecheck <= statescreated){
            EstadoAFD actual = estados.get(String.valueOf(statecheck));

            LinkedHashMap<String, List<GrammarExtended.ProductionWithPointer>> movimientos = new LinkedHashMap<>();

            // Mover el pointer por cada movimiento que se pueda hacer en el estado
            for (GrammarExtended.ProductionWithPointer item : actual.getItems()) {
                if (item.getPointer() < item.getSymbols().size()) {
                    String simbolo = item.getSymbols().get(item.getPointer());
                    GrammarExtended.ProductionWithPointer nuevo = new GrammarExtended.ProductionWithPointer(item.getSymbols(), item.getPointer() + 1);
                    movimientos.computeIfAbsent(simbolo, k -> new ArrayList<>()).add(nuevo);
                }
            }

            // Creacion de estados/generacion de transiciones
            for (Map.Entry<String, List<GrammarExtended.ProductionWithPointer>> transicion : movimientos.entrySet()) {
                String simbolo = transicion.getKey();
                List<GrammarExtended.ProductionWithPointer> nuevosItems = transicion.getValue();
                boolean encontrado = false;
                List<GrammarExtended.ProductionWithPointer> closureItems = null;
                List<GrammarExtended.ProductionWithPointer> itemsCompletos = new ArrayList<>(nuevosItems);

                // Verificacion de necesidad de agregar closure
                for (GrammarExtended.ProductionWithPointer item : nuevosItems) {
                    if (item.getPointer() < item.getSymbols().size()) {
                        String simboloActual = item.getSymbols().get(item.getPointer());
                        if (noTerminales.contains(simboloActual)) {
                            closureItems = closure(simboloActual, gext);
                        }
                    }
                }
                
                // Agregar closure al estado si fuera necesario
                if (closureItems != null) {
                    for(ProductionWithPointer itemclosure: closureItems){
                        itemsCompletos.add(itemclosure);
                    }
                }
                
                // Revision de estados creados para confirmar que es un nuevo estado o una transicion
                System.out.println("REVISANDO SI HAY TRANSICIONES DISPONIBLES PARA ESTADO " + statecheck);
                for (EstadoAFD existente : estados.values()) {
                    if (new HashSet<>(existente.getItems()).containsAll(itemsCompletos)) {
                        
                        System.out.println( statecheck + " - " + simbolo + " > "+ existente.getId());
                        encontrado = true;
                        break;
                    }
                }
                
                // Creacion de estado en caso de ser unico
                if (!encontrado) {
                    System.out.println("**CREANDO NUEVO ESTADO**");
                    statescreated++;
                    EstadoAFD nuevoEstado = new EstadoAFD(String.valueOf(statescreated));
                    System.out.println("Transición por símbolo: " + simbolo);
                    System.out.println("→ Nuevo estado: " + nuevoEstado.getId());

                    for (GrammarExtended.ProductionWithPointer item : itemsCompletos) {
                        nuevoEstado.agregarItem(item);
                    }
                    estados.put(nuevoEstado.getId(), nuevoEstado);
                    System.out.println("Items del estado " + nuevoEstado.getId() + ":");
                    for (GrammarExtended.ProductionWithPointer item : nuevoEstado.getItems()) {
                        System.out.println("  " + item); 
                    }
                    System.out.println(statecheck + " - " + simbolo + " > "+ statescreated);
                    System.out.println("----");
                }
            }
            statecheck++;

        }

        AFD afd = new AFD(null, null, null, "0", null);

        return estados;
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
        Map<String, EstadoAFD> estadosAFD = generarEstados(extendida, estado0);

        // 6. Imprimir todos los estados y sus transiciones
        System.out.println("\nEstados AFD generados:");
        for (EstadoAFD estado : estadosAFD.values()) {
            System.out.println("Estado " + estado.getId() + ":");
            for (ProductionWithPointer item : estado.getItems()) {
                System.out.println("  " + item);
            }
        }

    }
    
    
}

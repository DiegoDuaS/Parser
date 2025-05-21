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

import models.EstadoAFD;
import models.Grammar;
import models.GrammarExtended;
import models.GrammarExtended.ProductionWithPointer;
import static modules.automaton.extension.extenderGramatica;

public class automaton {

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


    public static Map<String, EstadoAFD> generarEstados(GrammarExtended gext, EstadoAFD estadoInicial) {
        Map<String, EstadoAFD> estados = new LinkedHashMap<>();
        Queue<EstadoAFD> pendientes = new LinkedList<>();
        Map<String, List<ProductionWithPointer>> closures = construirClosures(gext);
        List<String> noTerminales = gext.getNoTerminales();

        estados.put(estadoInicial.getNombre(), estadoInicial);
        pendientes.add(estadoInicial);
        

        int contadorEstado = 1;

        while (!pendientes.isEmpty()) {
            EstadoAFD actual = pendientes.poll();

            Map<String, List<GrammarExtended.ProductionWithPointer>> movimientos = new HashMap<>();

            for (GrammarExtended.ProductionWithPointer item : actual.getItems()) {
                if (item.getPointer() < item.getSymbols().size()) {
                    String simbolo = item.getSymbols().get(item.getPointer());
                    GrammarExtended.ProductionWithPointer nuevo = new GrammarExtended.ProductionWithPointer(item.getSymbols(), item.getPointer() + 1);
                    movimientos.computeIfAbsent(simbolo, k -> new ArrayList<>()).add(nuevo);
                }
            }

            for (Map.Entry<String, List<GrammarExtended.ProductionWithPointer>> transicion : movimientos.entrySet()) {
                String simbolo = transicion.getKey();
                List<GrammarExtended.ProductionWithPointer> nuevosItems = transicion.getValue();

                // Buscar si ya existe un estado con esos items
                boolean encontrado = false;
                for (EstadoAFD existente : estados.values()) {
                    if (new HashSet<>(existente.getItems()).containsAll(nuevosItems)) {
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    EstadoAFD nuevoEstado = new EstadoAFD(String.valueOf(contadorEstado++));
                    String simboloActual = "";
                    boolean found = false; 

                    System.out.println("Transición por símbolo: " + simbolo);
                    System.out.println("→ Nuevo estado: " + nuevoEstado.getNombre());

                    for (GrammarExtended.ProductionWithPointer item : nuevosItems) {
                        nuevoEstado.agregarItem(item);
                        // Mostrar símbolo donde está el puntero (si no está al final)
                        if (item.getPointer() < item.getSymbols().size()) {
                            simboloActual = item.getSymbols().get(item.getPointer());
                            if (noTerminales.contains(simboloActual)){
                                found = true;
                            }
                        } else {
                            System.out.print("  <-- Puntero al final");
                        }
                    }

                    if(found){
                        System.out.println("FOUND");
                        //List<ProductionWithPointer> closureItems = closures.get(simboloActual);
                        //for (ProductionWithPointer closure: closureItems){
                          //  nuevoEstado.agregarItem(closure);
                        //}
                    }

                    estados.put(nuevoEstado.getNombre(), nuevoEstado);
                    pendientes.add(nuevoEstado);

                    System.out.println("----");
                }

            }
        }

        return estados;
    }

    public static List<GrammarExtended.ProductionWithPointer> closure(String simbolo, GrammarExtended gext) {
        List<GrammarExtended.ProductionWithPointer> resultado = new ArrayList<>();
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
        EstadoAFD estado0 = automaton.crearEstadoInicial(extendida);
        System.out.println("\nEstado inicial:");
        System.out.println(estado0.getNombre());
        for (ProductionWithPointer item : estado0.getItems()) {
            System.out.println("  " + item);
        }

        // 5. Generar todos los estados del AFD
        Map<String, EstadoAFD> estadosAFD = automaton.generarEstados(extendida, estado0);

        // 6. Imprimir todos los estados y sus transiciones
        System.out.println("\nEstados AFD generados:");
        for (EstadoAFD estado : estadosAFD.values()) {
            System.out.println("Estado " + estado.getNombre() + ":");
            for (ProductionWithPointer item : estado.getItems()) {
                System.out.println("  " + item);
            }
        }

        List<GrammarExtended.ProductionWithPointer> cierre = closure("S'", extendida);
        for (GrammarExtended.ProductionWithPointer item : cierre) {
            System.out.println(item);
        }

    }

    


    
}

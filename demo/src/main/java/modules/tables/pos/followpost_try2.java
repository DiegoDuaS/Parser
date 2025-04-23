package modules.tables.pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.FollowRule;
import models.Grammar;


public class followpost_try2 {
    
    private Grammar grammar;
    private static final int MAX_ITERATIONS = 5;
    private final int MAX_PRODUCCIONES_POR_NOTERMINAL;

    private int calcularMaxProduccionesPorNoTerminal() {
        Map<String, List<String>> producciones = grammar.getProductions();
        int maxProducciones = 0;
        
        for (List<String> produccionesDeNoTerminal : producciones.values()) {
            if (produccionesDeNoTerminal != null) {
                maxProducciones = Math.max(maxProducciones, produccionesDeNoTerminal.size());
            }
        }
        
        return maxProducciones * 3;
    }
    
    public followpost_try2(Grammar grammar) {
        this.grammar = grammar;
        this.MAX_PRODUCCIONES_POR_NOTERMINAL = calcularMaxProduccionesPorNoTerminal();
    }
    
    public Map<String, List<String>> ingresando_produccion() {

        // Paso 1: Obtener el HashMap con las producciones
        Map<String, List<String>> producciones = grammar.getProductions();
        
        // Paso 3: Duplicar el HashMap dos veces
        Map<String, List<String>> originalMap = duplicarHashMap(producciones);
        Map<String, List<String>> mapaParaModificar = duplicarHashMap(producciones);
        
        boolean cambios = true;
        int iteraciones = 0;
        
        while (cambios && iteraciones < MAX_ITERATIONS) {
            cambios = false;
            Map<String, List<String>> nuevoMapa = new HashMap<>();
            
            // Paso 2: Leer cada valor de cada llave del hashmap
            for (Map.Entry<String, List<String>> entry : originalMap.entrySet()) {
                String noTerminal = entry.getKey();
                List<String> produccionesActuales = entry.getValue();
                Set<String> nuevasProduccionesParaNoTerminal = new HashSet<>();
                
                // Agregar las producciones originales
                nuevasProduccionesParaNoTerminal.addAll(produccionesActuales);
                
                // Expandir cada producción una vez
                for (String produccion : produccionesActuales) {
                    // Tokenizar la producción
                    String[] simbolos = produccion.trim().split("\\s+");
                    
                    // Solo sustituir el primer no terminal encontrado
                    boolean sustituido = false;
                    for (int i = 0; i < simbolos.length && !sustituido; i++) {
                        String simbolo = simbolos[i];
                        
                        if (grammar.getNoTerminales().contains(simbolo)) {
                            // Evitar expandir el mismo no terminal para prevenir recursión
                            if (!simbolo.equals(noTerminal)) {
                                List<String> produccionesDelNoTerminal = originalMap.get(simbolo);
                                
                                if (produccionesDelNoTerminal != null) {
                                    // Crear nuevas producciones sustituyendo este no terminal
                                    for (String sustitucion : produccionesDelNoTerminal) {
                                        String nuevaProduccion = sustituirEnPosicion(simbolos, i, sustitucion);
                                        nuevasProduccionesParaNoTerminal.add(nuevaProduccion);
                                    }
                                    sustituido = true;
                                }
                            }
                        }
                    }
                }
                
                // Limitar el número de producciones por no terminal
                if (nuevasProduccionesParaNoTerminal.size() > MAX_PRODUCCIONES_POR_NOTERMINAL) {
                    List<String> limitadas = new ArrayList<>(nuevasProduccionesParaNoTerminal);
                    nuevasProduccionesParaNoTerminal.clear();
                    nuevasProduccionesParaNoTerminal.addAll(limitadas.subList(0, MAX_PRODUCCIONES_POR_NOTERMINAL));
                }
                
                // Verificar si hubo cambios
                List<String> listaAnterior = mapaParaModificar.get(noTerminal);
                if (listaAnterior == null || nuevasProduccionesParaNoTerminal.size() != listaAnterior.size() ||
                    !nuevasProduccionesParaNoTerminal.containsAll(listaAnterior)) {
                    cambios = true;
                }
                
                nuevoMapa.put(noTerminal, new ArrayList<>(nuevasProduccionesParaNoTerminal));
            }
            
            // Actualizar los mapas para la siguiente iteración
            mapaParaModificar = nuevoMapa;
            originalMap = duplicarHashMap(mapaParaModificar);
            iteraciones++;
        }
        
        return mapaParaModificar;
    }
    
    private String expandirNoTerminalesATerminales(String cadena) {
        String[] simbolos = cadena.trim().split("\\s+");
        Set<String> simbolosVistos = new HashSet<>();
        List<String> resultado = new ArrayList<>();
        
        for (int i = 0; i < simbolos.length; i++) {
            String simbolo = simbolos[i];
            
            if (grammar.getTerminales().contains(simbolo)) {
                if (!simbolosVistos.contains(simbolo)) {
                    resultado.add(simbolo);
                    simbolosVistos.add(simbolo);
                }
            } else if (grammar.getNoTerminales().contains(simbolo)) {
                List<String> producciones = grammar.getProductions().get(simbolo);
                for (String produccion : producciones) {
                    String[] produccionSimbolos = produccion.trim().split("\\s+");
                    boolean todoTerminales = true;
                    
                    for (String prod : produccionSimbolos) {
                        if (grammar.getNoTerminales().contains(prod)) {
                            todoTerminales = false;
                            break;
                        }
                    }
                    
                    if (todoTerminales) {
                        for (String terminal : produccionSimbolos) {
                            if (!simbolosVistos.contains(terminal)) {
                                resultado.add(terminal);
                                simbolosVistos.add(terminal);
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        // Ordenar para normalizar la salida
        Collections.sort(resultado);
        return String.join(" ", resultado);
    }
    
    // NUEVO MÉTODO - Fase 2: Calcular reglas FOLLOW
    public List<FollowRule> calcularFollowRules() {
        Map<String, List<String>> produccionesExpandidas = ingresando_produccion();
        Set<FollowRule> reglasFollow = new HashSet<>(); // Usar Set para evitar duplicados
        
        // Mapa para rastrear reglas ya vistas (normalización adicional)
        Map<String, Set<String>> reglasVistas = new HashMap<>();
        
        // Iterar sobre cada no terminal y sus producciones
        for (Map.Entry<String, List<String>> entry : produccionesExpandidas.entrySet()) {
            String llaveOrigen = entry.getKey(); // No terminal del lado izquierdo
            List<String> producciones = entry.getValue();
            
            // Analizar cada producción
            for (String produccion : producciones) {
                String[] simbolos = produccion.trim().split("\\s+");
                
                // Identificar todos los no terminales en la producción
                for (int i = 0; i < simbolos.length; i++) {
                    String simbolo = simbolos[i];
                    
                    // Si es un no terminal
                    if (grammar.getNoTerminales().contains(simbolo)) {
                        // Verificar si tiene algo a la derecha
                        if (i < simbolos.length - 1) {
                            // Caso 2: Tiene símbolos a la derecha
                            // Construir la cadena con todo lo que está a la derecha
                            StringBuilder derecha = new StringBuilder();
                            for (int j = i + 1; j < simbolos.length; j++) {
                                if (j > i + 1) {
                                    derecha.append(" ");
                                }
                                derecha.append(simbolos[j]);
                            }
                            
                            // Expandir no terminales a terminales y eliminar duplicados
                            String derechaExpandida = expandirNoTerminalesATerminales(derecha.toString());
                            
                            // Verificar si ya existe esta regla para este no terminal
                            if (!derechaExpandida.isEmpty()) {
                                String clave = simbolo + "|FIRST";
                                if (!reglasVistas.containsKey(clave)) {
                                    reglasVistas.put(clave, new HashSet<>());
                                }
                                
                                if (!reglasVistas.get(clave).contains(derechaExpandida)) {
                                    FollowRule reglaCaso2 = new FollowRule(
                                        simbolo, 
                                        "FIRST", 
                                        derechaExpandida
                                    );
                                    reglasFollow.add(reglaCaso2);
                                    reglasVistas.get(clave).add(derechaExpandida);
                                }
                            }
                        } else {
                            // Caso 3: No tiene nada a la derecha
                            String clave = simbolo + "|FOLLOW";
                            if (!reglasVistas.containsKey(clave)) {
                                reglasVistas.put(clave, new HashSet<>());
                            }
                            
                            if (!reglasVistas.get(clave).contains(llaveOrigen)) {
                                FollowRule reglaCaso3 = new FollowRule(
                                    simbolo, 
                                    "FOLLOW", 
                                    llaveOrigen
                                );
                                reglasFollow.add(reglaCaso3);
                                reglasVistas.get(clave).add(llaveOrigen);
                            }
                        }
                    }
                }
            }
        }
        
        // Convertir el Set a List para retornar
        return new ArrayList<>(reglasFollow);
    }
    
    private String sustituirEnPosicion(String[] simbolos, int posicion, String sustitucion) {
        StringBuilder resultado = new StringBuilder();
        
        for (int i = 0; i < simbolos.length; i++) {
            if (i == posicion) {
                resultado.append(sustitucion);
            } else {
                resultado.append(simbolos[i]);
            }
            
            if (i < simbolos.length - 1) {
                resultado.append(" ");
            }
        }
        
        return resultado.toString();
    }
    
    private Map<String, List<String>> duplicarHashMap(Map<String, List<String>> original) {
        Map<String, List<String>> copia = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : original.entrySet()) {
            String key = entry.getKey();
            List<String> valores = new ArrayList<>(entry.getValue());
            copia.put(key, valores);
        }
        
        return copia;
    }
    
    // Método para pruebas
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
        
        // Configurar la gramática de ejemplo
        grammar.agregarNoTerminal("S");
        grammar.agregarNoTerminal("P");
        grammar.agregarNoTerminal("Q");
        
        grammar.agregarTerminal("^");
        grammar.agregarTerminal("V");
        grammar.agregarTerminal("[");
        grammar.agregarTerminal("]");
        grammar.agregarTerminal("sentence");
        
        grammar.agregarProduccion("S", "S ^ P");
        grammar.agregarProduccion("S", "P");
        grammar.agregarProduccion("P", "P V Q");
        grammar.agregarProduccion("P", "Q");
        grammar.agregarProduccion("Q", "[ S ]");
        grammar.agregarProduccion("Q", "sentence");
        
        followpost_try2 follow = new followpost_try2(grammar);
        
        // Mostrar producciones expandidas
        Map<String, List<String>> produccionesExpandidas = follow.ingresando_produccion();
        System.out.println("Producciones expandidas:");
        for (Map.Entry<String, List<String>> entry : produccionesExpandidas.entrySet()) {
            System.out.println(entry.getKey() + " -> "+ entry.getValue() +"\n");
        }
        
        System.out.println("========================================");
        
            // Calcular reglas FOLLOW
        List<FollowRule> reglasFollow = follow.calcularFollowRules();

        // Separar las reglas FOLLOW en dos listas
        List<FollowRule> reglasFollowFollow = new ArrayList<>();
        List<FollowRule> reglasFollowFirst = new ArrayList<>();

        for (FollowRule regla : reglasFollow) {
            if (regla.getOperation().equals("FOLLOW")) {
                reglasFollowFollow.add(regla);
            } else if (regla.getOperation().equals("FIRST")) {
                reglasFollowFirst.add(regla);
            }
        }

        // Ordenar cada lista por no terminal
        Collections.sort(reglasFollowFollow, (r1, r2) -> r1.getNonTerminal().compareTo(r2.getNonTerminal()));
        Collections.sort(reglasFollowFirst, (r1, r2) -> r1.getNonTerminal().compareTo(r2.getNonTerminal()));

        // Mostrar primero las reglas FOLLOW-FOLLOW
        System.out.println("Reglas FOLLOW-FOLLOW:");
        for (FollowRule regla : reglasFollowFollow) {
            System.out.println(regla);
        }

        System.out.println("\nReglas FOLLOW-FIRST:");
        for (FollowRule regla : reglasFollowFirst) {
            System.out.println(regla);
        }
}
}
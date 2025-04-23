package modules.tables.pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Grammar;

public class followpost_try2 {
    
    private Grammar grammar;
    private static final int MAX_ITERATIONS = 5; // Límite más conservador
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
    
    public List<String> ingresando_produccion() {
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
        
        // Convertir el HashMap final a una lista de producciones
        List<String> resultado = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : mapaParaModificar.entrySet()) {
            String noTerminal = entry.getKey();
            for (String produccion : entry.getValue()) {
                resultado.add(noTerminal + " -> " + produccion);
            }
        }
        
        return resultado;
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
        List<String> produccionesExpandidas = follow.ingresando_produccion();
        
        System.out.println("Producciones expandidas:");
        for (String produccion : produccionesExpandidas) {
            System.out.println(produccion);
        }
    }
}
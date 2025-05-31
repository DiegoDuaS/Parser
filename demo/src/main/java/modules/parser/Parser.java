package modules.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import models.Grammar;
import models.ParsingTable;
import models.ReduceEntry;
import modules.error.ErrorHandler;

/**
 * Módulo de Parseo - Realiza el análisis sintáctico LR(0)
 * 
 * Responsabilidades:
 * - Ejecutar el algoritmo de parseo LR(0) usando las tablas ACTION y GOTO
 * - Manejar las operaciones SHIFT, REDUCE y ACCEPT
 * - Mantener el stack de parseo con estados y símbolos
 * - Generar árbol de derivación (opcional)
 * - Delegar errores al módulo de manejo de errores
 */
public class Parser {
    
    private ParsingTable parsingTable;
    private ErrorHandler errorHandler;
    private Stack<String> stateStack;    // Stack de estados
    private Stack<String> symbolStack;   // Stack de símbolos
    private List<String> inputTokens;    // Tokens de entrada
    private int currentTokenIndex;       // Índice del token actual
    private boolean accepted;            // Flag de aceptación
    private boolean debug;               // Flag para mostrar debugging
    
    public Parser(ParsingTable parsingTable) {
        this.parsingTable = parsingTable;
        this.errorHandler = new ErrorHandler();
        this.errorHandler.setParsingTable(parsingTable);
        this.stateStack = new Stack<>();
        this.symbolStack = new Stack<>();
        this.currentTokenIndex = 0;
        this.accepted = false;
        this.debug = true;
    }
    
    /**
     * Método principal de parseo
     * 
     * Proceso implementado:
     * 1. Inicializar stacks con estado inicial (0)
     * 2. Loop principal hasta ACCEPT o ERROR:
     *    - Obtener token actual
     *    - Consultar ACTION table con (estado_actual, token)
     *    - Ejecutar acción correspondiente (SHIFT/REDUCE/ACCEPT)
     * 3. Retornar resultado del parseo
     */
    public boolean parse(List<String> tokens) {
        this.inputTokens = new ArrayList<>(tokens);
        initializeParsing();
        
        if (debug) {
            System.out.println("\n=== INICIANDO PARSEO LR(0) ===");
            System.out.println("Input: " + String.join(" ", inputTokens));
            System.out.println("\nPASO A PASO:");
            printHeader();
        }
        
        while (!accepted && currentTokenIndex < inputTokens.size()) {
            String currentState = getCurrentState();
            String currentToken = getCurrentToken();
            String action = getAction(currentState, currentToken);
            
            if (debug) {
                printParserState(currentState, currentToken, action);
            }
            
            if (action == null || action.isEmpty()) {
                // Error sintáctico
                handleError(currentState, currentToken);
                return false;
            }
            
            if (action.equals("ACCEPT")) {
                executeAccept();
                break;
            } else if (action.startsWith("S")) {
                executeShift(action);
            } else if (action.startsWith("R")) {
                executeReduce(action);
            } else {
                handleError(currentState, currentToken);
                return false;
            }
        }
        
        if (debug && accepted) {
            System.out.println("\n✅ EL INPUT ES ACEPTADO");
        }
        
        return accepted;
    }
    
    /**
     * Inicializa el parser para una nueva entrada
     */
    private void initializeParsing() {
        stateStack.clear();
        symbolStack.clear();
        currentTokenIndex = 0;
        accepted = false;
        
        // Agregar estado inicial (0) al stack
        stateStack.push("0");
        
        // Agregar símbolo centinela $ si no existe
        if (!inputTokens.get(inputTokens.size() - 1).equals("$")) {
            inputTokens.add("$");
        }
    }
   
    /**
     * Ejecuta una operación SHIFT
     */
    private void executeShift(String action) {
        // Extraer número de estado de la acción (ej: "S5" -> "5")
        String newState = action.substring(1);
        String currentToken = getCurrentToken();
        
        // Push del símbolo actual al symbolStack
        symbolStack.push(currentToken);
        
        // Push del nuevo estado al stateStack
        stateStack.push(newState);
        
        // Avanzar al siguiente token
        currentTokenIndex++;
    }
    
    /**
     * Ejecuta una operación REDUCE
     */
    private void executeReduce(String action) {
        // Obtener información de la producción desde reduceDictionary
        ReduceEntry reduceInfo = parsingTable.getReduceDictionary().get(action);
        
        if (reduceInfo == null) {
            System.err.println("Error: No se encontró información para " + action);
            return;
        }
        
        String productionHead = reduceInfo.getProduction_head();
        String productionBody = reduceInfo.getProduction_value();
        
        // Contar elementos en el lado derecho de la producción
        String[] rhsSymbols = productionBody.trim().split("\\s+");
        int rhsLength = rhsSymbols.length;
        
        // Hacer pop de |producción| elementos de ambos stacks
        for (int i = 0; i < rhsLength; i++) {
            if (!stateStack.isEmpty()) stateStack.pop();
            if (!symbolStack.isEmpty()) symbolStack.pop();
        }
        
        // Hacer push del lado izquierdo de la producción al symbolStack
        symbolStack.push(productionHead);
        
        // Consultar GOTO table con (estado_actual, no_terminal)
        String currentState = getCurrentState();
        String gotoState = getGotoState(currentState, productionHead);
        
        if (gotoState != null && !gotoState.isEmpty()) {
            // Hacer push del nuevo estado al stateStack
            stateStack.push(gotoState);
        } else {
            System.err.println("Error: No se encontró transición GOTO para (" + currentState + ", " + productionHead + ")");
        }
    }
    
    /**
     * Ejecuta la operación ACCEPT
     */
    private void executeAccept() {
        accepted = true;
        if (debug) {
            System.out.println("Estado: " + getCurrentState() + " | Input: $ | Acción: ACCEPT");
        }
    }
     /**
     * Obtiene el token actual como string
     */
    private String getCurrentToken() {
        if (currentTokenIndex >= inputTokens.size()) {
            return "$";
        }
        return inputTokens.get(currentTokenIndex);
    }
    
    /**
     * Obtiene el estado actual del tope del stack
     */
    private String getCurrentState() {
        return stateStack.isEmpty() ? "0" : stateStack.peek();
    }
    
    /**
     * Consulta la tabla ACTION
     */
    private String getAction(String state, String symbol) {
        if (parsingTable.getActionTable().containsKey(state)) {
            return parsingTable.getActionTable().get(state).get(symbol);
        }
        return null;
    }
    
    /**
     * Consulta la tabla GOTO
     */
    private String getGotoState(String state, String nonTerminal) {
        if (parsingTable.getGoToTable().containsKey(state)) {
            return parsingTable.getGoToTable().get(state).get(nonTerminal);
        }
        return null;
    }
    
    /**
     * Maneja errores sintácticos
     */
    private void handleError(String currentState, String currentToken) {
        System.err.println("\n❌ ERROR SINTÁCTICO DETECTADO");
        System.err.println("Estado actual: " + currentState);
        System.err.println("Token actual: " + currentToken);
        System.err.println("Posición: " + currentTokenIndex);
        
        // Delegar al ErrorHandler para manejo más sofisticado
        errorHandler.handleSyntaxError(currentState, currentToken, currentTokenIndex, inputTokens);
    }
    
    /**
     * Imprime el encabezado de la tabla de debugging
     */
    private void printHeader() {
        System.out.printf("%-15s | %-20s | %-40s%n", "STACK", "INPUT", "ACTION");
        System.out.println("----------------+----------------------+------------------------------------------");
    }
    
    /**
     * Imprime el estado actual del parser (para debugging)
     */
    private void printParserState(String currentState, String currentToken, String action) {
        // Construir representación del stack
        StringBuilder stackStr = new StringBuilder();
        for (int i = 0; i < stateStack.size(); i++) {
            if (i < symbolStack.size()) {
                stackStr.append(stateStack.get(i)).append(symbolStack.get(i));
            } else {
                stackStr.append(stateStack.get(i));
            }
        }
        
        // Construir representación del input restante
        StringBuilder inputStr = new StringBuilder();
        for (int i = currentTokenIndex; i < inputTokens.size(); i++) {
            inputStr.append(inputTokens.get(i));
            if (i < inputTokens.size() - 1) inputStr.append(" ");
        }
        
        // Describir la acción
        String actionDescription = "";
        if (action != null) {
            if (action.equals("ACCEPT")) {
                actionDescription = "ACCEPT";
            } else if (action.startsWith("S")) {
                actionDescription = "SHIFT -> Estado " + action.substring(1);
            } else if (action.startsWith("R")) {
                ReduceEntry reduceInfo = parsingTable.getReduceDictionary().get(action);
                if (reduceInfo != null) {
                    actionDescription = action + ": Reduce " + reduceInfo.getProduction_head() + " -> " + reduceInfo.getProduction_value();
                } else {
                    actionDescription = action + ": Reduce";
                }
            }
        } else {
            actionDescription = "ERROR";
        }
        
        System.out.printf("%-15s | %-20s | %-40s%n", 
                         stackStr.toString(), 
                         inputStr.toString(), 
                         actionDescription);
    }
    
    // Getters para acceso externo
    public boolean isAccepted() {
        return accepted;
    }
    
    public Stack<String> getStateStack() {
        return stateStack;
    }
    
    public Stack<String> getSymbolStack() {
        return symbolStack;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * Método main con ejemplo de uso
     */
    public static void main(String[] args) {
        System.out.println("=== GENERANDO GRAMÁTICA Y TABLAS DE PARSEO ===");
        
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

      
    }
}
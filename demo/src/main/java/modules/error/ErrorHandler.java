package modules.error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.ParsingTable;

/**
 * Módulo de Manejo de Errores - Detecta y reporta errores sintácticos
 * 
 * Responsabilidades:
 * - Detectar errores sintácticos cuando no hay acción en las tablas
 * - Generar mensajes descriptivos de error con ubicación
 * - Implementar estrategias de recuperación de errores
 * - Sugerir posibles correcciones basadas en la gramática
 * - Mantener estadísticas de errores encontrados
 */
public class ErrorHandler {
    
    private ParsingTable parsingTable;
    private int errorCount;
    private boolean panicMode;
    private List<ErrorReport> errorHistory;
    
    public ErrorHandler() {
        this.errorCount = 0;
        this.panicMode = false;
        this.errorHistory = new ArrayList<>();
    }
    
    public void setParsingTable(ParsingTable parsingTable) {
        this.parsingTable = parsingTable;
    }
    
    /**
     * Maneja errores sintácticos durante el parseo
     * 
     * Proceso implementado:
     * 1. Incrementar contador de errores
     * 2. Determinar tipo de error (token inesperado, fin prematuro, etc.)
     * 3. Generar mensaje descriptivo con ubicación
     * 4. Intentar recuperación si es posible
     * 5. Decidir si continuar o abortar el parseo
     */
    public void handleSyntaxError(String currentState, String currentToken, int position, List<String> inputTokens) {
        // 1. Incrementar contador de errores
        errorCount++;
        
        // 2. Determinar tipo de error
        ErrorType errorType = determineErrorType(currentToken, position, inputTokens);
        
        // 3. Generar mensaje descriptivo
        String errorMessage = generateErrorMessage(currentState, currentToken, position, errorType);
        
        // 4. Obtener tokens esperados
        Set<String> expectedTokens = getExpectedTokens(currentState);
        
        // 5. Generar sugerencias
        List<String> suggestions = generateSuggestions(currentToken, expectedTokens);
        
        // 6. Crear reporte de error
        ErrorReport report = new ErrorReport(errorType, errorMessage, position, currentToken, expectedTokens, suggestions);
        errorHistory.add(report);
        
        // 7. Reportar error
        reportError(report);
        
        // 8. Intentar recuperación
        RecoveryAction recovery = attemptErrorRecovery(currentState, currentToken, position, inputTokens);
        
        System.err.println("Estrategia de recuperación recomendada: " + recovery);
    }
    
    /**
     * Determina el tipo específico de error sintáctico
     * 
     * Tipos de error detectados:
     * - TOKEN_INESPERADO: Token no válido en el contexto actual
     * - FIN_PREMATURO: Entrada terminó antes de completar el parseo
     * - SIMBOLO_FALTANTE: Se esperaba un símbolo específico
     * - SECUENCIA_INVALIDA: Secuencia de tokens no válida
     */
    private ErrorType determineErrorType(String currentToken, int position, List<String> inputTokens) {
        // Verificar si llegamos al final prematuramente
        if (position >= inputTokens.size() || currentToken.equals("$")) {
            return ErrorType.FIN_PREMATURO;
        }
        
        // Verificar si es el último token válido antes del final
        if (position == inputTokens.size() - 2 && inputTokens.get(inputTokens.size() - 1).equals("$")) {
            return ErrorType.SIMBOLO_FALTANTE;
        }
        
        // Por defecto, es un token inesperado
        return ErrorType.TOKEN_INESPERADO;
    }
      /**
     * Genera mensaje de error descriptivo
     * 
     * Proceso implementado:
     * 1. Obtener información de ubicación (posición)
     * 2. Describir qué se encontró vs qué se esperaba
     * 3. Generar información de contexto
     * 4. Formatear mensaje para el usuario
     */
    private String generateErrorMessage(String currentState, String currentToken, int position, ErrorType errorType) {
        StringBuilder message = new StringBuilder();
        
        // Mensaje base según tipo de error
        switch (errorType) {
            case TOKEN_INESPERADO:
                message.append("Token inesperado '").append(currentToken).append("'");
                break;
            case FIN_PREMATURO:
                message.append("Fin prematuro de la entrada");
                break;
            case SIMBOLO_FALTANTE:
                message.append("Símbolo faltante antes del final");
                break;
            case SECUENCIA_INVALIDA:
                message.append("Secuencia de tokens inválida");
                break;
        }
        
        // Agregar información de contexto
        message.append(" en la posición ").append(position);
        message.append(" (estado ").append(currentState).append(")");
        
        return message.toString();
    }
    
    /**
     * Obtiene lista de tokens/símbolos esperados en el estado actual
     * 
     * Proceso implementado:
     * 1. Consultar ACTION table para el estado actual
     * 2. Extraer todos los símbolos que tienen acciones definidas
     * 3. Filtrar y formatear para mostrar al usuario
     */
    private Set<String> getExpectedTokens(String currentState) {
        Set<String> expectedTokens = new HashSet<>();
        
        if (parsingTable != null && parsingTable.getActionTable().containsKey(currentState)) {
            expectedTokens.addAll(parsingTable.getActionTable().get(currentState).keySet());
        }
        
        // Remover entradas vacías o nulas
        expectedTokens.removeIf(token -> token == null || token.trim().isEmpty());
        
        return expectedTokens;
    }
    
    /**
     * Intenta recuperación de error usando diferentes estrategias
     * 
     * Estrategias implementadas:
     * 1. SKIP_TOKEN: Saltar el token problemático
     * 2. SYNC_SYMBOL: Buscar símbolo de sincronización (ej: ';', '}')
     * 3. POP_STACK: Hacer pop del stack hasta encontrar estado válido
     * 4. INSERT_TOKEN: Simular inserción de token faltante
     */
    public RecoveryAction attemptErrorRecovery(String currentState, String currentToken, int position, List<String> inputTokens) {
        // 1. Verificar si se puede insertar un token faltante
        Set<String> expectedTokens = getExpectedTokens(currentState);
        for (String expectedToken : expectedTokens) {
            if (canInsertToken(expectedToken, currentState)) {
                return RecoveryAction.INSERT_TOKEN;
            }
        }
        
        // 2. Buscar punto de sincronización hacia adelante
        int syncPoint = findSynchronizationPoint(position, inputTokens);
        if (syncPoint >= 0) {
            return RecoveryAction.SYNC_FORWARD;
        }
        
        // 3. Evaluar si saltar el token actual es viable
        if (position < inputTokens.size() - 1) {
            return RecoveryAction.SKIP_TOKEN;
        }
        
        // 4. Como último recurso, hacer pop del stack
        if (errorCount < 3) { // Solo si no hay demasiados errores
            return RecoveryAction.POP_STACK;
        }
        
        // 5. Si no hay recuperación viable, abortar
        return RecoveryAction.ABORT;
    }
    
    /**
     * Busca símbolos de sincronización hacia adelante en la entrada
     * 
     * Proceso implementado:
     * 1. Definir conjunto de símbolos de sincronización
     * 2. Buscar hacia adelante en inputTokens
     * 3. Retornar posición del símbolo encontrado
     */
    private int findSynchronizationPoint(int startPosition, List<String> inputTokens) {
        // Definir símbolos de sincronización comunes
        Set<String> syncSymbols = new HashSet<>();
        syncSymbols.add("]"); // Cierre de bloques
        syncSymbols.add("$"); // Final de entrada
        syncSymbols.add("sentence"); // Elementos básicos de la gramática
        
        // Buscar hacia adelante
        for (int i = startPosition + 1; i < inputTokens.size(); i++) {
            if (syncSymbols.contains(inputTokens.get(i))) {
                return i;
            }
        }
        
        return -1; // No se encontró punto de sincronización
    }
       /**
     * Valida si un token puede ser insertado para recuperación
     * 
     * Proceso implementado:
     * 1. Verificar si insertar el token permite continuar
     * 2. Consultar ACTION table con (currentState, tokenToInsert)
     * 3. Retornar viabilidad de la inserción
     */
    private boolean canInsertToken(String tokenToInsert, String currentState) {
        if (parsingTable == null) return false;
        
        // Verificar si hay una acción válida para este token en el estado actual
        String action = parsingTable.getActionTable()
                .getOrDefault(currentState, new java.util.HashMap<>())
                .get(tokenToInsert);
        
        return action != null && !action.trim().isEmpty();
    }
    
    /**
     * Genera sugerencias de corrección basadas en el contexto
     * 
     * Proceso implementado:
     * 1. Analizar tokens esperados
     * 2. Buscar tokens similares al encontrado
     * 3. Sugerir correcciones comunes (typos, símbolos faltantes)
     */
    private List<String> generateSuggestions(String unexpectedToken, Set<String> expectedTokens) {
        List<String> suggestions = new ArrayList<>();
        
        if (expectedTokens.isEmpty()) {
            suggestions.add("Verificar la sintaxis de la gramática");
            return suggestions;
        }
        
        // Sugerir tokens esperados
        if (expectedTokens.size() <= 3) {
            suggestions.add("Se esperaba uno de: " + String.join(", ", expectedTokens));
        } else {
            suggestions.add("Se esperaba un token válido. Tokens posibles: " + 
                          String.join(", ", expectedTokens).substring(0, Math.min(50, String.join(", ", expectedTokens).length())) + "...");
        }
        
        // Buscar similitudes simples (para errores de tipeo)
        for (String expected : expectedTokens) {
            if (isTypoLikely(unexpectedToken, expected)) {
                suggestions.add("¿Quiso decir '" + expected + "' en lugar de '" + unexpectedToken + "'?");
                break;
            }
        }
        
        // Sugerencias específicas por contexto
        if (expectedTokens.contains("]") && !expectedTokens.contains("[")) {
            suggestions.add("Parece que falta cerrar un bloque con ']'");
        }
        
        if (expectedTokens.contains("$")) {
            suggestions.add("La entrada parece estar incompleta");
        }
        
        return suggestions;
    }
    
    /**
     * Verifica si es probable que sea un error de tipeo
     */
    private boolean isTypoLikely(String actual, String expected) {
        if (actual == null || expected == null) return false;
        
        // Verificar longitud similar
        if (Math.abs(actual.length() - expected.length()) > 2) return false;
        
        // Verificar caracteres en común
        int commonChars = 0;
        for (char c : actual.toCharArray()) {
            if (expected.indexOf(c) >= 0) {
                commonChars++;
            }
        }
        
        // Si más del 60% de los caracteres coinciden, es probable un typo
        return (double) commonChars / Math.max(actual.length(), expected.length()) > 0.6;
    }
    
    /**
     * Reporta error al usuario con formato apropiado
     * 
     * Proceso implementado:
     * 1. Formatear mensaje para consola/log
     * 2. Incluir información de contexto
     * 3. Mostrar sugerencias si están disponibles
     */
    private void reportError(ErrorReport report) {
        System.err.println("\n" + "=".repeat(50));
        System.err.println("❌ ERROR SINTÁCTICO #" + errorCount);
        System.err.println("=".repeat(50));
        System.err.println("Tipo: " + report.getErrorType());
        System.err.println("Mensaje: " + report.getMessage());
        System.err.println("Posición: " + report.getPosition());
        System.err.println("Token encontrado: '" + report.getUnexpectedToken() + "'");
        
        if (!report.getExpectedTokens().isEmpty()) {
            System.err.println("Tokens esperados: " + report.getExpectedTokens());
        }
        
        if (!report.getSuggestions().isEmpty()) {
            System.err.println("\nSugerencias:");
            for (String suggestion : report.getSuggestions()) {
                System.err.println("  • " + suggestion);
            }
        }
        
        System.err.println("=".repeat(50));
    }
    
    /**
     * Determina si se debe continuar o abortar el parseo
     * 
     * Proceso implementado:
     * - Evaluar severidad del error
     * - Considerar número de errores acumulados
     * - Verificar si la recuperación fue exitosa
     */
    public boolean shouldContinueParsing() {
        // Decidir basado en:
        // - Número de errores (umbral máximo)
        // - Modo pánico activado
        // - Tipo de errores encontrados
        
        if (errorCount >= 10) {
            System.err.println("⚠️  Demasiados errores encontrados. Abortando parseo.");
            return false;
        }
        
        if (panicMode && errorCount >= 5) {
            System.err.println("⚠️  Modo pánico activado con múltiples errores. Abortando parseo.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Reinicia el estado de error para nuevo parseo
     */
    public void reset() {
        this.errorCount = 0;
        this.panicMode = false;
        this.errorHistory.clear();
    }
    
    /**
     * Obtiene resumen de errores encontrados
     */
    public String getErrorSummary() {
        if (errorCount == 0) {
            return "✅ No se encontraron errores sintácticos";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("❌ Resumen de errores encontrados:\n");
        summary.append("Total de errores: ").append(errorCount).append("\n");
        
        // Contar por tipo
        java.util.Map<ErrorType, Integer> countByType = new java.util.HashMap<>();
        for (ErrorReport report : errorHistory) {
            countByType.put(report.getErrorType(), 
                           countByType.getOrDefault(report.getErrorType(), 0) + 1);
        }
        
        for (java.util.Map.Entry<ErrorType, Integer> entry : countByType.entrySet()) {
            summary.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return summary.toString();
    }
    
    // Getters para información de estado
    public int getErrorCount() {
        return errorCount;
    }
    
    public boolean isInPanicMode() {
        return panicMode;
    }
    
    public List<ErrorReport> getErrorHistory() {
        return errorHistory;
    }
    
    // Clase interna para reportes de error
    public static class ErrorReport {
        private ErrorType errorType;
        private String message;
        private int position;
        private String unexpectedToken;
        private Set<String> expectedTokens;
        private List<String> suggestions;
        
        public ErrorReport(ErrorType errorType, String message, int position, 
                          String unexpectedToken, Set<String> expectedTokens, 
                          List<String> suggestions) {
            this.errorType = errorType;
            this.message = message;
            this.position = position;
            this.unexpectedToken = unexpectedToken;
            this.expectedTokens = expectedTokens;
            this.suggestions = suggestions;
        }
        
        // Getters
        public ErrorType getErrorType() { return errorType; }
        public String getMessage() { return message; }
        public int getPosition() { return position; }
        public String getUnexpectedToken() { return unexpectedToken; }
        public Set<String> getExpectedTokens() { return expectedTokens; }
        public List<String> getSuggestions() { return suggestions; }
    }
    
    // Enums para clasificación
    public enum ErrorType {
        TOKEN_INESPERADO,
        FIN_PREMATURO, 
        SIMBOLO_FALTANTE,
        SECUENCIA_INVALIDA
    }
    
    public enum RecoveryAction {
        SKIP_TOKEN,      // Saltar token actual
        SYNC_FORWARD,    // Buscar símbolo de sincronización
        POP_STACK,       // Hacer pop del stack
        INSERT_TOKEN,    // Insertar token faltante
        ABORT           // Abortar parseo
    }
}
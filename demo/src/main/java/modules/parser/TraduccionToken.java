package modules.parser;

import java.util.*;
import com.example.models.Token;

public class TraduccionToken {

    /**
     * Convierte una lista de tokens en listas de tipos de tokens separadas por EOL
     * 
     * @param tokens Lista de tokens del analizador léxico
     * @return Lista de listas donde cada sublista contiene los tipos de tokens
     *         de una línea, terminando con "EOF"
     */
    public static List<List<String>> traducirTokens(List<Token> tokens) {
        List<List<String>> resultado = new ArrayList<>();
        List<String> lineaActual = new ArrayList<>();

        for (Token token : tokens) {
            String tipo = token.getTipo();

            // Ignorar tokens de espacios en blanco o buffer léxico
            if (tipo.equals("lexbuf") || tipo.equals("WHITESPACE") || tipo.equals("SPACE")) {
                continue;
            }

            // Si encontramos EOL, finalizamos la línea actual
            if (tipo.equals("EOL")) {
                // Solo agregar la línea si no está vacía
                if (!lineaActual.isEmpty()) {
                    // lineaActual.add("EOF"); // Agregar EOF al final de cada línea
                    resultado.add(new ArrayList<>(lineaActual));
                    lineaActual.clear();
                }
            } else {
                // Agregar el tipo del token a la línea actual
                lineaActual.add(tipo);
            }
        }

        // Si queda una línea sin EOL al final, agregarla también
        if (!lineaActual.isEmpty()) {
            // lineaActual.add("EOF");
            resultado.add(lineaActual);
        }

        return resultado;
    }

    /**
     * Método para procesar una sola línea de tokens hasta encontrar EOL o EOF
     * 
     * @param tokens Lista de tokens del analizador léxico
     * @param inicio Índice desde donde comenzar a procesar
     * @return Par con la lista de tipos de tokens y el siguiente índice a procesar
     */
    public static ProcessResult procesarLineaUnica(List<Token> tokens, int inicio) {
        List<String> linea = new ArrayList<>();
        int i = inicio;

        while (i < tokens.size()) {
            Token token = tokens.get(i);
            String tipo = token.getTipo();

            // Si encontramos EOL, terminamos la línea
            if (tipo.equals("EOL")) {
                if (!linea.isEmpty()) {
                    linea.add("EOF");
                }
                return new ProcessResult(linea, i + 1);
            }

            // Agregar token válido
            linea.add(tipo);
            i++;
        }

        // Si llegamos al final sin EOL, agregar EOF
        if (!linea.isEmpty()) {
            linea.add("EOF");
        }

        return new ProcessResult(linea, i);
    }

    /**
     * Método de utilidad para imprimir las listas de tokens de forma legible
     * 
     * @param listasTokens Lista de listas de tipos de tokens
     */
    public static void imprimirListasTokens(List<List<String>> listasTokens) {
        System.out.println("=== LISTAS DE TOKENS GENERADAS ===");
        for (int i = 0; i < listasTokens.size(); i++) {
            System.out.println("Línea " + (i + 1) + ": " + listasTokens.get(i));
        }
    }

    /**
     * Clase auxiliar para retornar resultado del procesamiento de una línea
     */
    public static class ProcessResult {
        private final List<String> linea;
        private final int siguienteIndice;

        public ProcessResult(List<String> linea, int siguienteIndice) {
            this.linea = linea;
            this.siguienteIndice = siguienteIndice;
        }

        public List<String> getLinea() {
            return linea;
        }

        public int getSiguienteIndice() {
            return siguienteIndice;
        }
    }

    /**
     * Método de ejemplo y prueba
     */
    public static void main(String[] args) {
        // Crear tokens de ejemplo basados en tu input
        List<Token> tokensEjemplo = Arrays.asList(
                new Token("12345", "NUMBER"),
                new Token(" ", "lexbuf"),
                new Token("+", "PLUS"),
                new Token(" ", "lexbuf"),
                new Token("67890", "NUMBER"),
                new Token(" ", "lexbuf"),
                new Token("*", "TIMES"),
                new Token(" ", "lexbuf"),
                new Token("(", "LPAREN"),
                new Token("42", "NUMBER"),
                new Token(" ", "lexbuf"),
                new Token("/", "DIV"),
                new Token(" ", "lexbuf"),
                new Token("6", "DIGIT"),
                new Token(")", "RPAREN"),
                new Token(" ", "lexbuf"),
                new Token("-", "MINUS"),
                new Token(" ", "lexbuf"),
                new Token("99", "NUMBER"),
                new Token(" ", "lexbuf"),
                new Token("variable_1", "VARIABLE"),
                new Token("\n", "EOL"),
                new Token("variable_1", "VARIABLE"),
                new Token(" ", "lexbuf"),
                new Token("=", "EQUALS"),
                new Token(" ", "lexbuf"),
                new Token("'Hello World!'", "COMMENT"),
                new Token("\n", "EOL"),
                new Token("if", "WORD"),
                new Token(" ", "lexbuf"),
                new Token("(", "LPAREN"),
                new Token("x", "WORD"),
                new Token(" ", "lexbuf"),
                new Token(">=", "LESSEQ"),
                new Token(" ", "lexbuf"),
                new Token("100", "NUMBER"),
                new Token(" ", "lexbuf"),
                new Token("&", "APPERSAND"),
                new Token("&", "APPERSAND"),
                new Token(" ", "lexbuf"),
                new Token("y", "WORD"),
                new Token(" ", "lexbuf"),
                new Token("<", "MORE"),
                new Token(" ", "lexbuf"),
                new Token("50", "NUMBER"),
                new Token(")", "RPAREN"),
                new Token(" ", "lexbuf"),
                new Token("{", "LHOOK"),
                new Token("\n", "EOL"));

        // Procesar tokens
        List<List<String>> resultado = traducirTokens(tokensEjemplo);

        // Imprimir resultados
        imprimirListasTokens(resultado);
    }
}
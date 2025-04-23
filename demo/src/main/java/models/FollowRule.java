package models;

import java.util.Objects;

/**
 * Clase para representar una regla FOLLOW en una gramática formal.
 * Cada regla tiene la forma FOLLOW(nonTerminal) = operation(argument)
 * donde operation puede ser "FIRST" o "FOLLOW".
 */
public class FollowRule {
    private String nonTerminal;  // El no terminal que estamos estudiando
    private String operation;    // "FIRST" o "FOLLOW"
    private String argument;     // Lo que está a la derecha o el no terminal origen

    /**
     * Constructor para una nueva regla FOLLOW.
     * 
     * @param nonTerminal El no terminal para el cual se calcula el FOLLOW
     * @param operation La operación a aplicar ("FIRST" o "FOLLOW")
     * @param argument El argumento de la operación
     */
    public FollowRule(String nonTerminal, String operation, String argument) {
        this.nonTerminal = nonTerminal;
        this.operation = operation;
        this.argument = argument;
    }

    /**
     * Obtiene el no terminal de esta regla FOLLOW.
     * 
     * @return El no terminal
     */
    public String getNonTerminal() {
        return nonTerminal;
    }

    /**
     * Obtiene la operación de esta regla FOLLOW.
     * 
     * @return La operación ("FIRST" o "FOLLOW")
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Obtiene el argumento de esta regla FOLLOW.
     * 
     * @return El argumento
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Retorna una representación en string de la regla FOLLOW.
     * 
     * @return String en formato "FOLLOW(nonTerminal) = operation(argument)"
     */
    @Override
    public String toString() {
        return "FOLLOW(" + nonTerminal + ") = " + operation + "(" + argument + ")";
    }

    /**
     * Compara esta regla FOLLOW con otro objeto para determinar igualdad.
     * 
     * @param o El objeto a comparar
     * @return true si los objetos son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowRule that = (FollowRule) o;
        return Objects.equals(nonTerminal, that.nonTerminal) &&
               Objects.equals(operation, that.operation) &&  
               Objects.equals(argument, that.argument);
    }

    /**
     * Genera un código hash para esta regla FOLLOW.
     * 
     * @return El código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(nonTerminal, operation, argument);
    }
}
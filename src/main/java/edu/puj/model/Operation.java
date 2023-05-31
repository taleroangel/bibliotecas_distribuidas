package edu.puj.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Operation {
    OperationType type;
    Long value;

    /**
     * Crear una operación a partir de una string en el formato '82114;S'
     * @param input String con formato value;operation
     * @return Operación que representa la string
     */
    public static Operation fromString(String input) {
        String[] parts = input.split(";");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de cadena inválido: " + input);
        }

        long value;
        try {
            value = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor numérico inválido: " + parts[0]);
        }

        char operationChar = parts[1].charAt(0);
        OperationType type;
        try {
            type = OperationType.fromCommand(operationChar);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de operación inválido: " + operationChar);
        }

        return new Operation(type, value);
    }
}

package edu.puj.model;


import lombok.Getter;

@Getter
public enum OperationType {
    SOLICITAR('S'),
    DEVOLVER('D'),
    RENOVAR('R');

    final char command;

    OperationType(char command) {
        this.command = command;
    }

    /**
     * Crear una operación a partir de su comando
     * @param command Caracter que representa el comando
     * @return Operación
     */
    public static OperationType fromCommand(char command) {
        for (OperationType type : OperationType.values()) {
            if (type.getCommand() == command) {
                return type;
            }
        }
        throw new IllegalArgumentException("Operación no válida: " + command);
    }
}

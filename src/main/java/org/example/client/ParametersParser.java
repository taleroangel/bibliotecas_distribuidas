package org.example.client;

import lombok.Getter;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.impl.*;
import net.sourceforge.argparse4j.inf.*;

public class ParametersParser {

    @Getter
    private int port;
    @Getter
    private String address;
    @Getter
    private int timeout;
    @Getter
    private int retries;
    @Getter
    private String file;

    private ArgumentParser parser = ArgumentParsers.newFor("Client").build();

    ParametersParser() {
        parser.addArgument("-p", "--port")
                .type(Integer.class)
                .required(true)
                .help("Puerto del servidor");

        parser.addArgument("-a", "--address")
                .type(String.class)
                .required(true)
                .help("Dirección IP del servidor");

        parser.addArgument("-t", "--timeout")
                .type(Integer.class)
                .setDefault(5000)
                .help("Timeout en milisegundos (por defecto 5000)");

        parser.addArgument("-r", "--retries")
                .type(Integer.class)
                .setDefault(5)
                .help("Número de intentos (por defecto 5)");

        parser.addArgument("-f", "--file")
                .type(String.class)
                .required(true)
                .help("Path hacia el archivo a leer");
    }

    void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace ns = parser.parseArgs(args);
        this.port = ns.getInt("port");
        this.address = ns.getString("address");
        this.timeout = ns.getInt("timeout");
        this.retries = ns.getInt("retries");
        this.file = ns.getString("file");

        // Loggearlos
        System.out.println("Puerto: " + this.port);
        System.out.println("Dirección IP: " + this.address);
        System.out.println("Timeout: " + this.timeout);
        System.out.println("Intentos: " + this.retries);
        System.out.println("Archivo: " + this.file);
    }
}

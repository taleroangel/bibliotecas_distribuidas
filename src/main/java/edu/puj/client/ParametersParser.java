package edu.puj.client;

import lombok.Getter;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;

/**
 * Parse command line arguments
 */
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

    private final ArgumentParser PARSER = ArgumentParsers.newFor("Client").build();

    ParametersParser() {
        PARSER.addArgument("-p", "--port")
                .type(Integer.class)
                .required(true)
                .help("RequestManager TCP Port");

        PARSER.addArgument("-a", "--address")
                .type(String.class)
                .required(true)
                .help("RequestManager ip address");

        PARSER.addArgument("-t", "--timeout")
                .type(Integer.class)
                .setDefault(5000)
                .help("Timeout in milliseconds (5000 by default)");

        PARSER.addArgument("-r", "--retries")
                .type(Integer.class)
                .setDefault(5)
                .help("Number of retries (5 by default)");

        PARSER.addArgument("-f", "--file")
                .type(String.class)
                .required(true)
                .help("Input file path");
    }

    void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace argumentsNamespace = PARSER.parseArgs(args);
        this.port = argumentsNamespace.getInt("port");
        this.address = argumentsNamespace.getString("address");
        this.timeout = argumentsNamespace.getInt("timeout");
        this.retries = argumentsNamespace.getInt("retries");
        this.file = argumentsNamespace.getString("file");
    }

    @Override
    public String toString() {
        return String.format("ParametersParser={port:%d, ip:%s, timeout:%d, retries:%d, file:%s}",
                this.port, this.address, this.timeout, this.retries, this.file);
    }
}

package edu.puj.manager;

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
    private int clients;

    private final ArgumentParser PARSER = ArgumentParsers.newFor("LoadManager").build();

    ParametersParser() {
        PARSER.addArgument("-p", "--port")
                .type(Integer.class)
                .required(true)
                .help("Connection TCP Port");

        PARSER.addArgument("-c", "--clients")
                .type(Integer.class)
                .setDefault(10)
                .help("Number of concurrent clients (10 by default)");
    }

    void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace argumentsNamespace = PARSER.parseArgs(args);
        this.port = argumentsNamespace.getInt("port");
        this.clients = argumentsNamespace.getInt("clients");
    }

    @Override
    public String toString() {
        return String.format("ParametersParser={port:%d, clients=%d}", this.port, this.clients);
    }
}

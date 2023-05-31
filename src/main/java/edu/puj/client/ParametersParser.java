package edu.puj.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;

@Getter
@ToString
public class ParametersParser {
    private int port;
    private String address;
    private int timeout;
    private int retries;
    private String file;

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    private final ArgumentParser PARSER = ArgumentParsers.newFor("Client").build();

    public ParametersParser() {
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

    public void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace argumentsNamespace = PARSER.parseArgs(args);
        this.port = argumentsNamespace.getInt("port");
        this.address = argumentsNamespace.getString("address");
        this.timeout = argumentsNamespace.getInt("timeout");
        this.retries = argumentsNamespace.getInt("retries");
        this.file = argumentsNamespace.getString("file");
    }
}

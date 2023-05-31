package edu.puj.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;

@Getter
@ToString
public class ParametersParser {

    private Long port;
    private Integer clients;
    private Long publish;

    private Long subscribe;

    @ToString.Exclude
    @Getter(AccessLevel.NONE)
    private final ArgumentParser PARSER = ArgumentParsers.newFor("LoadManager").build();

    ParametersParser() {
        PARSER.addArgument("-p", "--port")
                .type(Long.class)
                .required(true)
                .help("Client connection TCP Port");

        PARSER.addArgument("-s", "--subscribe")
                .type(Long.class)
                .required(true)
                .help("Worker connection TCP Port for publishing");

        PARSER.addArgument("-w", "--publish")
                .type(Long.class)
                .required(true)
                .help("Worker connection TCP Port for subscribing results");

        PARSER.addArgument("-c", "--clients")
                .type(Integer.class)
                .setDefault(15)
                .help("Number of concurrent clients (15 by default)");
    }

    public void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace argumentsNamespace = PARSER.parseArgs(args);
        this.port = argumentsNamespace.getLong("port");
        this.publish = argumentsNamespace.getLong("publish");
        this.subscribe = argumentsNamespace.getLong("subscribe");
        this.clients = argumentsNamespace.getInt("clients");
    }
}

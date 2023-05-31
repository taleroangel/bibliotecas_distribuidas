package edu.puj.worker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import net.sourceforge.argparse4j.*;
import net.sourceforge.argparse4j.inf.*;


@Getter
@ToString
public class ParametersParser {

    private String manager;

    private Long subscribe;
    private Long publish;
    private String database;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private final ArgumentParser PARSER = ArgumentParsers.newFor("Client").build();

    ParametersParser() {
        PARSER.addArgument("-m", "--manager")
                .type(String.class)
                .required(true)
                .help("RequestManager ip");

        PARSER.addArgument("-s", "--subscribe")
                .type(Long.class)
                .required(true)
                .help("RequestManager publisher port");

        PARSER.addArgument("-p", "--publish")
                .type(Long.class)
                .required(true)
                .help("Response publishing port");

        PARSER.addArgument("-d", "--database")
                .type(String.class)
                .setDefault(5000)
                .help("Database full ip address and port");
    }

    public void parseParameters(String[] args) throws ArgumentParserException {
        // Obtener los parámetros
        Namespace argumentsNamespace = PARSER.parseArgs(args);
        this.manager = argumentsNamespace.getString("manager");
        this.subscribe = argumentsNamespace.getLong("subscribe");
        this.publish = argumentsNamespace.getLong("publish");
        this.database = argumentsNamespace.getString("database");
    }
}

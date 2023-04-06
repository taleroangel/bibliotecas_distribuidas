package org.example.load_manager;

import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.util.ArrayList;
import java.util.Random;

public class LoadManager {

    public static void main(String[] args) {
        try {
            // 0. Parsear los parámetros
            ParametersParser arguments = new ParametersParser();
            arguments.parseParameters(args);
            System.out.println("INFO/ARGS:\t" + arguments);

            // 1. Crear los thread pools
            final var requestHandler = new RequestHandler(
                    arguments.getPort(), arguments.getClients());

            requestHandler.start();
            requestHandler.join();

        } catch (ArgumentParserException e) {
            e.getParser().handleError(e);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

package edu.puj.worker;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.zeromq.ZMQException;

import java.io.IOException;
import java.util.Arrays;

public class Worker {

    public static void main(String[] args) throws IllegalArgumentException {
        try {

            // 0. Parsear los argumentos
            ParametersParser arguments = new ParametersParser();
            arguments.parseParameters(args);
            System.out.println("INFO/ARGS:\t" + arguments);

            // 1. Mostrar la dirección IP
            String connectionUrl = String.format("tcp://%s", arguments.getManager());
            System.out.println("INFO/SERV:\t" + connectionUrl);
            System.out.println("INFO/DB:\t" + arguments.getDatabase());

            // 2. Crear el database querier
            final var querier = new CouchDBQuerierI(arguments.getDatabase());

            // 2. Crear el request handler
            final var requestHandler = new RequestHandler(
                    arguments.getManager(),
                    arguments.getSubscribe(),
                    arguments.getPublish(),
                    querier
            );

            requestHandler.start();
            requestHandler.join();

        } catch (ArgumentParserException e) {
            e.getParser().handleError(e);
            throw new IllegalArgumentException("Invalid Arguments: " + Arrays.toString(args));
        } catch (ZMQException e) {
            throw new RuntimeException("Failed to bind socket: " + e);
        } catch (InterruptedException e) {
            System.err.println("Interrupted Thread");
        }
    }

}

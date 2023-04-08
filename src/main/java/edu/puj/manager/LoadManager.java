package edu.puj.manager;

import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.zeromq.ZMQException;

import java.util.Arrays;

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
            throw new IllegalArgumentException("Invalid Arguments: " + Arrays.toString(args));
        } catch (ZMQException e) {
            throw new RuntimeException("Failed to bind socket: " + e);
        } catch (InterruptedException ignored) {
            System.err.println("Interrupted Thread");
        }
    }
}

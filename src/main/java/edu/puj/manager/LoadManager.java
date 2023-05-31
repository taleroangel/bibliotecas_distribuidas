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

            // Crear el controlador de recursos
            final var resourceController = new ResourceAssignationController(arguments.getPublish(), arguments.getSubscribe());
            final var resourceControllerThread = new Thread(resourceController);

            // Crear el manejador de solicitudes
            final var requestHandler = new RequestHandler(arguments.getPort(), arguments.getClients(), resourceController);

            // Iniciar el controlador de recursos
            resourceControllerThread.start();

            // Iniciar el manejador de solitudes
            requestHandler.start();
            requestHandler.join();

            // Detener el controlador de recursos
            resourceControllerThread.interrupt();
            resourceControllerThread.join();

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

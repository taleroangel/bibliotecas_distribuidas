package org.example.client;


import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.example.exceptions.UnsuccessfullRequest;

import java.util.ArrayList;


public class Client {
    private static final String ENDPOINT = "tcp://localhost:5555";


    public static void main(String[] args) {

        // 0. Parsear los parámetros
        ParametersParser arguments = new ParametersParser();
        try {
            arguments.parseParameters(args);
        } catch (ArgumentParserException e) {
            e.getParser().handleError(e);
            System.exit(1);
        }

        // 1.  Obtener los request
        ArrayList<String> requests = TxtReader.read(arguments.getFile());

        // 2.  Crear la request
        RequestManager rqst = new RequestManager(
                arguments.getTimeout(),
                arguments.getRetries(),
                String.format("tcp://%s:%d", arguments.getAddress(), arguments.getPort()));

        // 3. Enviar las request y recibir respuesta
        try {
            for (String request : requests) {
                System.out.println("Enviando request: " + request);
                String response = rqst.createRequest(request);
                System.out.println(response);
            }
        } catch (UnsuccessfullRequest ignored) {
            System.err.println("No hubo respuesta del servidor...");
        }
    }
}

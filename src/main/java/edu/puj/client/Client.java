package edu.puj.client;


import net.sourceforge.argparse4j.inf.ArgumentParserException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;


public class Client {

    public static void main(String[] args) throws IllegalArgumentException, IOException {
        try {
            // 0. Parsear los parámetros
            ParametersParser arguments = new ParametersParser();
            arguments.parseParameters(args);
            System.out.println("INFO/ARGS:\t" + arguments);

            // 0.1 Mostrar la dirección IP
            String connectionUrl = String.format(
                    "tcp://%s:%d", arguments.getAddress(), arguments.getPort());
            System.out.println("INFO/SERV:\t" + connectionUrl);

            // 1.  Obtener los request
            ArrayList<String> requests = FileReader.readLines(arguments.getFile());

            // 2.  Crear la request
            RequestManager rqst = new RequestManager(
                    arguments.getTimeout(),
                    arguments.getRetries(),
                    connectionUrl
            );

            // 3. Enviar las request y recibir respuesta
            for (String request : requests) {
                System.out.println("\n" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
                System.out.println("INFO/REQ:\t" + request);
                String response = rqst.createRequest(request);
                System.out.println("INFO/REP:\t" + response);
            }

        } catch (ArgumentParserException e) {
            e.getParser().handleError(e);
            throw new IllegalArgumentException("Invalid Arguments: " + Arrays.toString(args));
        }
    }
}

package org.example.client;


import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.example.exceptions.UnsuccessfullRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;


public class Client {
    private static final String ENDPOINT = "tcp://localhost:5555";

    public static void main(String[] args) {
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
            System.exit(1);

        } catch (IOException e) {
            System.err.println("ERR: Failed to read from input file");
            e.printStackTrace();
            System.exit(1);

        } catch (IllegalArgumentException e) {
            System.err.println("ERR: Input file format is invalid");
            e.printStackTrace();
            System.exit(1);

        } catch (UnsuccessfullRequest ignored) {
            System.err.println("ERR: Request couldn't be sent, server did not respond");
            System.exit(1);

        }
    }
}

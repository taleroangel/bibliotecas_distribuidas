package edu.puj.client;

import edu.puj.exceptions.UnableToCreateSocket;
import lombok.NonNull;
import edu.puj.exceptions.SocketException;
import edu.puj.exceptions.UnsuccessfullRequest;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * Build and make request to the server
 */
public class RequestManager {

    private final int REQUEST_TIMEOUT;
    private final int REQUEST_RETRIES;
    private final String SERVER_ENDPOINT;

    public RequestManager(int timeout, int retries, String endpoint) {
        this.REQUEST_TIMEOUT = timeout;
        this.REQUEST_RETRIES = retries;
        this.SERVER_ENDPOINT = endpoint;
    }

    public @NonNull String createRequest(String operation) {
        // Crear un nuevo contexto de ZeroMQ
        try (ZContext ctx = new ZContext()) {
            // Crear un socket de tipo Request REQ
            ZMQ.Socket requestSocket = ctx.createSocket(SocketType.REQ);

            // Verificar que el socket se haya creado
            if (requestSocket == null) {
                throw new UnableToCreateSocket();
            }

            // Conectarse al Gestor de Carga
            requestSocket.connect(SERVER_ENDPOINT);

            // Crear un poller con tamaño de 1
            ZMQ.Poller poller = ctx.createPoller(1);
            // Registrar el poller
            poller.register(requestSocket, ZMQ.Poller.POLLIN);

            // Enviar el contenido de la petición UTF-8
            requestSocket.send(operation.getBytes(ZMQ.CHARSET));

            for (int retriesLeft = 0;
                 retriesLeft < REQUEST_RETRIES && !Thread.currentThread().isInterrupted();
                 retriesLeft++) {

                // Recibir la respuesta (timeout en ms)
                int pollerResult = poller.poll(REQUEST_TIMEOUT);

                // Si hay error
                if (pollerResult == -1) {
                    throw new SocketException();
                } else if (pollerResult == 1) {
                    break; // Ya hubo respuesta
                }

                // Show retry
                System.out.printf("INFO/ERR:\tRetrying (%d/%d)%n", retriesLeft + 1, REQUEST_RETRIES);
            }

            // Verificar si hay algo que se pueda leer
            if (poller.pollin(0)) {
                return requestSocket.recvStr();
            } else {
                throw new UnsuccessfullRequest(); // No hubo respuesta
            }
        }
    }
}


package org.example.client;

import lombok.NonNull;
import org.example.exceptions.SocketException;
import org.example.exceptions.UnableToConnectException;
import org.example.exceptions.UnableToCreateSocket;
import org.example.exceptions.UnsuccessfullRequest;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class RequestManager {

    private final int REQUEST_TIMEOUT;
    private final int REQUEST_RETRIES;
    private final String SERVER_ENDPOINT;

    RequestManager(int timeout, int retries, String endpoint) {
        this.REQUEST_TIMEOUT = timeout;
        this.REQUEST_RETRIES = retries;
        this.SERVER_ENDPOINT = endpoint;
    }

    @NonNull String createRequest(String operation) {
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
            System.out.println("Petición enviada");

            for (int retriesLeft = REQUEST_RETRIES;
                 retriesLeft > 0 && !Thread.currentThread().isInterrupted();
                 retriesLeft--) {

                // Recibir la respuesta (timeout en ms)
                int pollerResult = poller.poll(REQUEST_TIMEOUT);

                // Si hay error
                if (pollerResult == -1) {
                    System.err.println("Fallo en el Socket de Comunicación");
                    throw new SocketException();
                } else if (pollerResult == 1) {
                    System.out.println("Respuesta recibida");
                    break; // Ya hubo respuesta
                }

                System.err.println("No hubo respuesta, reintentando");
            }

            // Verificar si hay algo que se pueda leer
            if (poller.pollin(0)) {
                return requestSocket.recvStr();
            } else {
                System.err.println("No hubo respuesta del servidor");
                throw new UnsuccessfullRequest(); // No hubo respuesta
            }
        }
    }
}


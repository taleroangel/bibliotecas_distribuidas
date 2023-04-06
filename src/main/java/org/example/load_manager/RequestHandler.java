package org.example.load_manager;

import org.example.exceptions.SocketException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestHandler extends Thread {

    final int SERVER_PORT;
    final int POLL_SIZE;

    RequestHandler(int port, int pollSize) {
        this.SERVER_PORT = port;
        this.POLL_SIZE = pollSize;
    }

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            // Crear un socket de response
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            String connectionString = String.format("tcp://*:%d", SERVER_PORT);
            socket.bind(connectionString);
            System.out.println("INFO/BIND:\t" + connectionString);

            // Crear el poller y registrar el socket
            ZMQ.Poller poller = context.createPoller(POLL_SIZE);
            System.out.println("INFO/POLL:\t" + poller.getSize());
            poller.register(socket, ZMQ.Poller.POLLIN);

            // While infinito (O hasta que se interrumpa el hilo)
            while (!Thread.currentThread().isInterrupted()) {
                // Bloquear el hilo con el pollers
                System.out.println("\n" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
                System.out.println("INFO/WAIT:\tBlocked");
                int pollResult = poller.poll();

                // Resultado del poll
                if (pollResult < 0) { // Hubo una falla en el socket
                    throw new SocketException();
                } else if (pollResult > 0) { // Hubo más de un socket que recibió una petición
                    for (int pollIndex = 0; pollIndex < poller.getSize(); pollIndex++) {
                        // Process incoming requests
                        if (poller.pollin(pollIndex)) {
                            // Receive the request
                            String request = socket.recvStr();
                            System.out.println("\n" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
                            System.out.println("INFO/RECV:\t" + request);

                            // TODO: Procesar la request

                            String response = "OK";
                            socket.send(response.getBytes(ZMQ.CHARSET));
                            System.out.println("INFO/SEND:\t" + response);
                        }
                    }
                }
            }
        }
    }
}

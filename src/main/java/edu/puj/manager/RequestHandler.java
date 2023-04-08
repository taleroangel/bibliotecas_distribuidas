package edu.puj.manager;

import edu.puj.exceptions.SocketException;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestHandler extends Thread {

    static final int CHECK_TIMER = 5000;

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
            poller.register(socket, ZMQ.Poller.POLLIN);

            // While infinito (O hasta que se interrumpa el hilo)
            System.out.println("INFO/START:\t" + LocalDateTime.now());
            while (!Thread.currentThread().isInterrupted()) {
                // Bloquear el hilo con el pollers
                int pollResult = poller.poll(CHECK_TIMER);

                // Resultado del poll
                if (pollResult < 0) { // Hubo una falla en el socket
                    throw new SocketException();
                } else if (pollResult > 0) { // Hubo más de un socket que recibió una petición
                    for (int pollIndex = 0; pollIndex < poller.getSize(); pollIndex++) {
                        // Process incoming requests
                        if (poller.pollin(pollIndex)) {
                            // Receive the request
                            String request = socket.recvStr();

                            // TODO: Procesar la request

                            String response = "OK";
                            socket.send(response.getBytes(ZMQ.CHARSET));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            System.out.println("INFO/STOP:\t" + LocalDateTime.now());
        }
    }
}

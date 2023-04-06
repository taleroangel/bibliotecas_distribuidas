package org.example.load_manager;

import org.example.exceptions.SocketException;
import org.zeromq.ZContext;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import java.util.Random;

public class LoadManager {

    static final int POLL_SIZE = 10;

    public static void main(String[] argv) throws Exception {
        try (ZContext context = new ZContext()) {
            // Create a ZMQ socket of type REP
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");

            // Create a Poller object and add the REP socket to it
            ZMQ.Poller poller = context.createPoller(POLL_SIZE);
            poller.register(socket, ZMQ.Poller.POLLIN);

            while (!Thread.currentThread().isInterrupted()) {
                // Wait for incoming requests
                int pollResult = poller.poll();

                if (pollResult < 0) { // Hubo una falla en el socket
                    throw new SocketException();
                } else if (pollResult > 0) { // Hubo más de un socket que recibió una petición
                    for (int pollIndex = 0; pollIndex < poller.getSize(); pollIndex++) {
                        // Process incoming requests
                        if (poller.pollin(pollIndex)) {
                            // Receive the request
                            String request = socket.recvStr();
                            System.out.println("Recibido: " + request);
                            // TODO: Procesar la request
                            socket.send("OK".getBytes(ZMQ.CHARSET));
                        }
                    }
                }
            }
        }
    }
}

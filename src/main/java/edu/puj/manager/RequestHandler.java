package edu.puj.manager;

import edu.puj.exceptions.SocketException;
import edu.puj.model.Operation;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;

public class RequestHandler extends Thread {

    public static final int CHECK_TIMER = 5000;

    public final Long SERVER_PORT;
    private final int POLL_SIZE;

    private final ResourceAssignationController controller;

    RequestHandler(Long port, int pollSize, ResourceAssignationController controller) {
        this.SERVER_PORT = port;
        this.POLL_SIZE = pollSize;

        // Crear el controlador
        this.controller = controller;
    }

    private ZMQ.Socket socket;

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            // Crear un socket de response
            socket = context.createSocket(SocketType.REP);

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
                            System.out.println("\nINFO/REQ\t" + request);

                            // Convertir el request
                            var operation = Operation.fromString(request);

                            String response = "OK"; // Respuesta
                            switch (operation.getType()) {
                                case RENOVAR:
                                case DEVOLVER:
                                    // Encolar la petición
                                    controller.enqueueOperation(operation);
                                    // No esperan una respuesta
                                    break;

                                case SOLICITAR:
                                    //TODO: Aún no implementada

                                default:
                                    response = "FAIL"; // Cambiar la respuesta a fallida
                                    System.err.println("ERR\tOperación no soportada");
                                    break;
                            }

                            // Enviar la respuesta
                            socket.send(response.getBytes(ZMQ.CHARSET));
                            System.out.println("INFO/RES\t" + response);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            if (socket != null) {
                socket.close();
            }

            System.out.println("INFO/STOP:\t" + LocalDateTime.now());
        }
    }
}

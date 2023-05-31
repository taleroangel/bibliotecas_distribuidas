package edu.puj.worker;

import edu.puj.exceptions.ItemNotFoundException;
import edu.puj.model.Libro;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class RequestHandler extends Thread {

    private final String MANAGER_SUBSCRIBE;
    private final String MANAGER_PUBLISH;

    private final IDatabaseQuerier databaseQuerier;

    public RequestHandler(String managerAddress, Long subscribePort, Long publishPort, IDatabaseQuerier querier) {
        MANAGER_SUBSCRIBE = String.format("tcp://%s:%d", managerAddress, subscribePort);
        MANAGER_PUBLISH = String.format("tcp://%s:%d", managerAddress, publishPort);
        databaseQuerier = querier;
    }

    private ZMQ.Socket listen;
    private ZMQ.Socket response;

    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            // Crear el socket de escuchar
            listen = context.createSocket(SocketType.SUB);
            listen.connect(MANAGER_SUBSCRIBE);

            // Suscribirse a los tópicos
            listen.subscribe("DEVOLVER");
            listen.subscribe("RENOVAR");
            listen.subscribe("SOLICITAR");

            // Crear el socket de respuesta
            response = context.createSocket(SocketType.PUB);
            response.connect(MANAGER_PUBLISH);

            while (!Thread.currentThread().isInterrupted()) {
                // Show the topic
                String topic = listen.recvStr();
                String message = listen.recvStr();
                System.out.println("\nINFO/REC\t" + topic + " " + message);

                // Failed response as default
                String responseStr = "FAIL";

                // Get book ID
                boolean successUpdate = false;
                Long libroId = Long.parseLong(message);

                try {
                    Libro libro = databaseQuerier.getLibro(libroId);
                    var calendar = Calendar.getInstance();

                    switch (topic) {
                        case "DEVOLVER":
                            // Solicitud no es posible
                            if (!libro.getPrestado()) {
                                break;
                            }

                            libro.setPrestado(false);
                            libro.setFechaEntrega("");

                            // Make update
                            successUpdate = databaseQuerier.updateLibro(libro);
                            break;

                        case "RENOVAR":

                            // Solicitud no es posible
                            if (!libro.getPrestado()) {
                                break;
                            }

                            // Update the date
                            calendar = Calendar.getInstance();
                            calendar.setTime(libro.getDate());
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);
                            libro.setDate(calendar.getTime());

                            // Make update
                            successUpdate = databaseQuerier.updateLibro(libro);
                            break;

                        case "SOLICITAR":

                            // Solicitud no es posible
                            if (libro.getPrestado()) {
                                break;
                            }

                            // Get current date + 1 week
                            calendar = Calendar.getInstance();
                            calendar.add(Calendar.WEEK_OF_YEAR, 1);

                            // Add it to book
                            libro.setPrestado(true);
                            libro.setDate(calendar.getTime());

                            successUpdate = databaseQuerier.updateLibro(libro);
                            break;

                        default:
                            System.err.println("ERR\tComando no soportado");
                            break;
                    }

                    // Prompt new response
                    responseStr = successUpdate ? "OK" : "FAIL";

                } catch (ItemNotFoundException e) {
                    System.err.println("ERR/BOOK\tDoes not exist");
                } catch (IOException e) {
                    System.err.println("ERR/INET\tFailed I/O");
                }

                // Send the response
                response.sendMore(responseStr.getBytes(ZMQ.CHARSET));
                response.send(message.getBytes(ZMQ.CHARSET));
                System.out.println("INFO/RES\t" + responseStr + ' ' + message);
            }

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (listen != null) {
                listen.close();
            }

            if (response != null) {
                response.close();
            }

            System.out.println("INFO/STOP:\t" + LocalDateTime.now());
        }
    }
}

package edu.puj.manager;

import edu.puj.model.Operation;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResourceAssignationController implements Runnable {

    public final Long PUBLISHER_PORT;
    public final Long SUBSCRIBER_PORT;

    public ResourceAssignationController(Long publisherPort, Long subscriberPort) {
        this.PUBLISHER_PORT = publisherPort;
        this.SUBSCRIBER_PORT = subscriberPort;

        pendingOperations = new LinkedBlockingQueue<>();
        queuedOperations = new LinkedBlockingQueue<>();
        responseSet = Collections.synchronizedSet(new HashSet<>());
        workerMap = Collections.synchronizedMap(new HashMap<>());
    }

    private final BlockingQueue<Operation> pendingOperations;

    private final BlockingQueue<Operation> queuedOperations;

    private final Set<Long> responseSet;

    enum WorkerStatus {
        UNUSED,
        WAITING,
        OK,
        FAIL
    }

    private final Map<String, WorkerStatus> workerMap;

    public void enqueueOperation(Operation operation) {
        pendingOperations.add(operation);
        System.out.println("INFO/RAC (Queued)\t" + operation);
    }

    public Boolean makeSolicitar(Operation operation) {
        while (workerMap.size() == 0) ; //Busy Wait

        // Buscar un worker libre
        String worker;
        do {
            final var randomIndex = new Random().nextInt(workerMap.size());
            worker = new ArrayList<>(workerMap.keySet()).get(randomIndex);
        } while (workerMap.get(worker) != WorkerStatus.UNUSED);


        final var operationString = operation.getValue().toString();

        // Enviar la petición
        publisher.sendMore(worker.getBytes(ZMQ.CHARSET));
        publisher.send(operationString.getBytes(ZMQ.CHARSET));
        System.out.println("INFO/RAC (Now)\t" + worker + ' ' + operationString);

        // Colocar en estado ocupado
        workerMap.put(worker, WorkerStatus.WAITING);

        // Espere la respuesta
        while (workerMap.get(worker) == WorkerStatus.WAITING) ;

        // Tomar el status
        final var status = workerMap.get(worker);

        // Actualizar el estatus
        workerMap.put(worker, WorkerStatus.UNUSED);

        // Retorna la respuesta
        return status == WorkerStatus.OK;
    }

    private ZMQ.Socket publisher;

    private ZMQ.Socket subscriber;

    @Override
    public void run() {

        // Create auxiliary threads
        final var operationsCheckThread = new Thread(new QueuedOperationsManager());
        operationsCheckThread.setDaemon(true);

        final var responseCheckThread = new Thread(new ResponseChecker());
        responseCheckThread.setDaemon(true);

        try (ZMQ.Context context = ZMQ.context(1)) {

            // Create publisher
            publisher = context.socket(SocketType.PUB);
            publisher.bind("tcp://localhost:" + PUBLISHER_PORT);

            // Start auxiliary daemons
            operationsCheckThread.start();
            responseCheckThread.start();

            while (!Thread.currentThread().isInterrupted()) {
                // Parse the operation in the pending operations
                Operation operation = pendingOperations.take();
                String topic = operation.getType().name();

                // Publish
                String message = operation.getValue().toString();
                publisher.sendMore(topic.getBytes(ZMQ.CHARSET));
                publisher.send(message.getBytes(ZMQ.CHARSET));

                // Queue it to queuedOperations
                queuedOperations.add(operation);
                System.out.println("INFO/RAC (Published)\t" + topic + ": " + message);
            }

        } catch (ZMQException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } finally {
            if (publisher != null) {
                publisher.close();
            }

            System.out.println("INFO/STOP:\t" + this.getClass().getSimpleName());
            operationsCheckThread.interrupt();
            responseCheckThread.interrupt();
        }
    }

    class QueuedOperationsManager implements Runnable {
        public static final int SLEEP_TIME = 5000;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {

                    // Get the current operation
                    final var operation = queuedOperations.take();

                    // Check if not resolved
                    if (!responseSet.contains(operation.getValue())) {
                        System.out.println("INFO/RM\tREATTEMPT " + operation.getValue());
                        pendingOperations.add(operation); // If operation recieved no response
                    } else {
                        responseSet.remove(operation.getValue());
                    }

                    // Sleep for some time
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ResponseChecker implements Runnable {
        @Override
        public void run() {
            try (ZMQ.Context context = ZMQ.context(1)) {

                // Create subscriber
                subscriber = context.socket(SocketType.SUB);
                subscriber.bind("tcp://localhost:" + SUBSCRIBER_PORT);

                // Subscribe to topics
                subscriber.subscribe("OK");
                subscriber.subscribe("FAIL");
                subscriber.subscribe("UNIQUE");

                // Topicos de los workers
                subscriber.subscribe("CONNECT");
                subscriber.subscribe("DISCONNECT");

                System.out.println("INFO/RC\tListening");

                while (!Thread.currentThread().isInterrupted()) {
                    // Get the result
                    String result = subscriber.recvStr();

                    if (result.equals("OK") || result.equals("FAIL")) {
                        // Get the values
                        Long value = Long.parseLong(subscriber.recvStr());

                        // Queue the response
                        responseSet.add(value);

                        // Get the operation
                        System.out.println("INFO/WORKER\t" + result + ' ' + value);

                    } else if (result.equals("UNIQUE")) {

                        final String[] tokens = subscriber.recvStr().split(" ");
                        final var operationStatus = tokens[0];
                        final var uuid = tokens[1];


                        // Actualizar el worker
                        System.out.println("UNIQUE/RES\t" + operationStatus + ' ' + uuid);
                        workerMap.put(uuid, Objects.equals(operationStatus, "OK") ? WorkerStatus.OK : WorkerStatus.FAIL);

                    } else {
                        String workerId = subscriber.recvStr();

                        switch (result) {
                            case "CONNECT":
                                if (!workerMap.containsKey(workerId)) {
                                    workerMap.put(workerId, WorkerStatus.UNUSED);
                                    System.out.println("WORKER/IN\t" + workerId);
                                }
                                break;

                            case "DISCONNECT":
                                System.out.println("WORKER/OUT\t" + workerId);
                                workerMap.remove(workerId);
                                break;

                            default:
                                System.out.println("WORKER\tUNKNOWN");
                                break;
                        }
                    }
                }
            } finally {
                if (subscriber != null) {
                    subscriber.close();
                }
            }
        }
    }
}
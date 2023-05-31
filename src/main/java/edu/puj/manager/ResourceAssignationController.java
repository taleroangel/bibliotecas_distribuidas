package edu.puj.manager;

import edu.puj.model.Operation;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

public class ResourceAssignationController implements Runnable {

    public final Long PUBLISHER_PORT;
    public final Long SUBSCRIBER_PORT;

    public ResourceAssignationController(Long publisherPort, Long subscriberPort) {
        this.PUBLISHER_PORT = publisherPort;
        this.SUBSCRIBER_PORT = subscriberPort;

        pendingOperations = new LinkedBlockingQueue<>();
        queuedOperations = new LinkedBlockingQueue<>();
        responseSet = Collections.synchronizedSet(new HashSet<>());
    }

    private final BlockingQueue<Operation> pendingOperations;

    private final BlockingQueue<Operation> queuedOperations;

    private final Set<Long> responseSet;

    public void enqueueOperation(Operation operation) {
        pendingOperations.add(operation);
        System.out.println("INFO/RAC (Queued)\t" + operation);
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
        public static final int SLEEP_TIME = 1000;

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

                while (!Thread.currentThread().isInterrupted()) {
                    // Get the result
                    String result = subscriber.recvStr();

                    // Get the values
                    Long value = Long.parseLong(subscriber.recvStr());

                    // Queue the response
                    responseSet.add(value);

                    // Get the operation
                    System.out.println("INFO/WORKER\t" + result + ' ' + value);
                }
            } finally {
                if (subscriber != null) {
                    subscriber.close();
                }
            }
        }
    }
}
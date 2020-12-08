package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.MessagesHandler;
import com.borlehandro.networks.snake.messages.factory.MessageFactory;
import com.borlehandro.networks.snake.model.SendTask;
import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;

import static com.borlehandro.networks.snake.protobuf.SnakesProto.GameMessage.TypeCase.*;

/**
 * Send directed messages to players
 */
public class NetworkActionsManager extends Thread {
    private final MessageFactory messageFactory = MessageFactory.getInstance();
    private final DatagramSocket socket;
    private final ArrayDeque<SendTask> messagesQueue = new ArrayDeque<>();
    private final HashMap<SendTask, Long> waitResponseMessages = new HashMap<>();
    private final ArrayDeque<SnakesProto.GameMessage> multicastQueue = new ArrayDeque<>();
    private int myId = -1;
    private MessagesHandler handler;
    private final int port;
    private static final SocketAddress MULTICAST_ADDRESS = new InetSocketAddress("239.192.0.4", 9192);

    private final PlayersServersRepository repository = PlayersServersRepository.getInstance();

    public NetworkActionsManager(MessagesHandler handler, int port) throws SocketException {
        this.handler = handler;
        // Todo fix port
        socket = new DatagramSocket(port);
        this.port = port;
    }

    public void changeMessageHandler(MessagesHandler messagesHandler) {
        // Todo test and fix
        synchronized (messagesQueue) {
            this.handler = messagesHandler;
        }
    }

    @Override
    public void run() {
        while (!interrupted()) {
            // Todo test synchronized
            // Send unicast
            synchronized (messagesQueue) {

//                    // System.err.println("-----Messages-----");
//                    messagesQueue.forEach(// System.err::println);
//                    // System.err.println("------------------");

                if (!messagesQueue.isEmpty()) {
                    var sendTask = messagesQueue.pollFirst();
                    var message = sendTask.getMessage();
                    System.err.println("in q: " + sendTask.getMessage().getTypeCase());
                    // Debug


                    if (message.getTypeCase().equals(SnakesProto.GameMessage.TypeCase.ACK))
                        System.err.println("Send ACK_MESSAGE " + System.currentTimeMillis());
                    if (message.getTypeCase().equals(SnakesProto.GameMessage.TypeCase.JOIN))
                        System.err.println("Send JOIN " + System.currentTimeMillis());
//                    if (sendTask.getMessage().getTypeCase().equals(MessageType.GAME_STATE_MESSAGE))
//                        System.err.println("Send game state message to " + sendTask.getMessage().getReceiverId());

                    var bytes = message.toByteArray();

                    var datagram = new DatagramPacket(
                            bytes,
                            bytes.length,
                            sendTask.getReceiverAddress());
                    try {
                        socket.send(datagram);
                        System.err.println("Send" + message.getTypeCase() + " on " + System.currentTimeMillis() + " to " + sendTask.getMessage().getReceiverId());
                        // Todo test
                        System.err.println("81");
                        synchronized (this) {
                            repository.updateLastSentMessageTimeMillis(sendTask.getMessage().getReceiverId(), System.currentTimeMillis(), false);
                        }
                        System.err.println("83");
                        if (!sendTask.getMessage().getTypeCase().equals(ACK) &&
                                !sendTask.getMessage().getTypeCase().equals(PING)) {
                            synchronized (waitResponseMessages) {
                                waitResponseMessages.put(sendTask, System.currentTimeMillis());
                            }
                        }
                        System.err.println("90");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.err.println("94");
                }
            }
            // Receive
            try {
                // Try to use sync on received time
                socket.setSoTimeout(50);
                byte[] datagramBuffer = new byte[5120]; // 5 Kb
                var receivedPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
                socket.receive(receivedPacket);
                // Todo test parsing
                byte[] bytes = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
                SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(bytes);
                System.err.println("Receive :" + message.getTypeCase() + " from " + message.getSenderId() + " time: " + System.currentTimeMillis());
                if (message.getTypeCase().equals(ACK)) {
                    System.err.println("Receive ack sender: " + message.getSenderId() + " time: " + System.currentTimeMillis());
                    synchronized (waitResponseMessages) {
                        System.err.println("Ack sync");
                        waitResponseMessages.entrySet().removeIf(entry ->
                                entry.getKey().getMessage().getMsgSeq() == message.getMsgSeq()
                        );
                    }
                } else if (!message.getTypeCase().equals(ANNOUNCEMENT) &&
                        !message.getTypeCase().equals(JOIN) &&
                        !message.getTypeCase().equals(PING)) {
                    // Todo test fake id!
                    synchronized (messagesQueue) {
                        System.err.println("message Queue 121");
                        // Add Ack Message to queue
                        messagesQueue.addLast(new SendTask(
                                messageFactory.createAckMessage(message.getMsgSeq(), myId, message.getSenderId()),
                                receivedPacket.getSocketAddress()
                        ));
                        System.err.println("message Queue 126");
                    }
                }
                if (message.getTypeCase().equals(JOIN))
                    System.err.println("Receive join " + System.currentTimeMillis());
                // Todo move handling in the top
                var socketAddress = new InetSocketAddress(receivedPacket.getAddress(), receivedPacket.getPort());
                handler.handleMessage(message, socketAddress);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Send multicast
            // Queue is always empty for client
            synchronized (multicastQueue) {
                if (!multicastQueue.isEmpty()) {
                    var message = multicastQueue.pollFirst();
                    var bytes = message.toByteArray();
                    System.err.println("Multicast send: " + Arrays.toString(bytes));
                    var datagram = new DatagramPacket(bytes,
                            bytes.length,
                            MULTICAST_ADDRESS);
                    try {
                        socket.send(datagram);
                        // Todo test
                        // repository.updateAllSentMessageTimes(System.currentTimeMillis());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Todo test synchronized
    public void putSendTask(SendTask message) {
        System.err.println("Try to put " + message.getMessage().getTypeCase());
        synchronized (messagesQueue) {
            System.err.println("162");
            messagesQueue.addLast(message);
            // System.err.println("I put in queue " + message.getMessage().getTypeCase());
            System.err.println("165");
        }
    }

    public void putMulticast(SnakesProto.GameMessage message) {
        synchronized (multicastQueue) {
            multicastQueue.addLast(message);
        }
    }

    public HashMap<SendTask, Long> getWaitResponseMessages() {
        return waitResponseMessages;
    }

    public int getPort() {
        return port;
    }

    public void setMyId(int id) {
        this.myId = id;
    }

}
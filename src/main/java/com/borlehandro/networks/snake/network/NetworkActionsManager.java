package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.MessagesHandler;
import com.borlehandro.networks.snake.model.SendTask;
import com.borlehandro.networks.snake.messages.Message;
import com.borlehandro.networks.snake.messages.MessageParser;
import com.borlehandro.networks.snake.messages.MessageType;
import com.borlehandro.networks.snake.messages.action.AckMessage;
import com.borlehandro.networks.snake.messages.state.AnnouncementMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Send directed messages to players
 */
public class NetworkActionsManager extends Thread {
    private final DatagramSocket socket;
    private final ArrayDeque<SendTask> messagesQueue = new ArrayDeque<>();
    private final HashMap<SendTask, Long> waitResponseMessages = new HashMap<>();
    private final ArrayDeque<AnnouncementMessage> multicastQueue = new ArrayDeque<>();
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

    public synchronized void changeMessageHandler(MessagesHandler messagesHandler) {
        // Todo test and fix
        synchronized (messagesQueue) {
            this.handler = messagesHandler;
        }
    }

    @Override
    public void run() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        while (!interrupted()) {
            // Send unicast
            synchronized (messagesQueue) {

//                    // System.err.println("-----Messages-----");
//                    messagesQueue.forEach(// System.err::println);
//                    // System.err.println("------------------");

                if (!messagesQueue.isEmpty()) {
                    var sendTask = messagesQueue.pollFirst();
                    var messageJson = gson.toJson(sendTask.getMessage());
                    // System.err.println("in q: " + sendTask.getMessage().getType());
                    // Debug


                    if (sendTask.getMessage().getType().equals(MessageType.ACK_MESSAGE))
                        System.err.println("Send ACK_MESSAGE " + System.currentTimeMillis());
                    if (sendTask.getMessage().getType().equals(MessageType.JOIN_MESSAGE))
                        System.err.println("Send JOIN " + System.currentTimeMillis());
//                    if (sendTask.getMessage().getType().equals(MessageType.GAME_STATE_MESSAGE))
//                        System.err.println("Send game state message to " + sendTask.getMessage().getReceiverId());

                    var datagram = new DatagramPacket(
                            messageJson.getBytes(),
                            messageJson.length(),
                            sendTask.getReceiverAddress());
                    try {
                        socket.send(datagram);
                        System.err.println("Send"+ sendTask.getMessage().getType() + "on " + System.currentTimeMillis() + " to " + sendTask.getMessage().getReceiverId());
                        // Todo test
                        // System.err.println("67");
                        repository.updateLastSentMessageTimeMillis(sendTask.getMessage().getReceiverId(), System.currentTimeMillis(), false);
                        // System.err.println("69");
                        if (!sendTask.getMessage().getType().equals(MessageType.ACK_MESSAGE) &&
                                !sendTask.getMessage().getType().equals(MessageType.PING_MESSAGE)) {
                            synchronized (waitResponseMessages) {
                                waitResponseMessages.put(sendTask, System.currentTimeMillis());
                            }
                        }
                        // System.err.println("75");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // System.err.println("message Queue 50 off");
                }
            }
            // Receive
            try {
                // Try to use sync on received time
                socket.setSoTimeout(50);
                byte[] datagramBuffer = new byte[5120]; // 5 Kb
                var receivedPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
                socket.receive(receivedPacket);
                String s = new String(datagramBuffer, 0, receivedPacket.getLength());
                Message message = MessageParser.parseMessage(s);
                System.err.println("Receive :" + message.getType() + " from " + message.getSenderId() + " time: " + System.currentTimeMillis());
                // Todo Add response message in the queue
                if (message.getType().equals(MessageType.ACK_MESSAGE)) {
                    System.err.println("Receive ack sender: " + message.getSenderId() + " time: " + System.currentTimeMillis());
                    synchronized (waitResponseMessages) {
                        System.err.println("Ack sync");
                        waitResponseMessages.entrySet().removeIf(entry ->
                                entry.getKey().getMessage().getMessageNumber() == message.getMessageNumber()
                        );
                    }
                } else if (!message.getType().equals(MessageType.ANNOUNCEMENT_MESSAGE) &&
                        !message.getType().equals(MessageType.JOIN_MESSAGE) &&
                        !message.getType().equals(MessageType.PING_MESSAGE)) {
                    // Todo test fake id!
                    synchronized (messagesQueue) {
                        // System.err.println("message Queue 120");
                        messagesQueue.addLast(new SendTask(
                                new AckMessage(message.getMessageNumber(), myId, message.getReceiverId()),
                                receivedPacket.getSocketAddress()
                        ));
                        // System.err.println("message Queue 120 off");
                    }
                }
                if (message.getType().equals(MessageType.JOIN_MESSAGE))
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
                    var messageJson = gson.toJson(message);
                    var datagram = new DatagramPacket(messageJson.getBytes(),
                            messageJson.length(),
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

    public void putSendTask(SendTask message) {
        // System.err.println("Try to put " + message.getMessage().getType());
        synchronized (messagesQueue) {
            // System.err.println("message Queue 142");
            messagesQueue.addLast(message);
            // System.err.println("I put in queue " + message.getMessage().getType());
            // System.err.println("message Queue 142 off");
        }
    }

    public void putMulticast(AnnouncementMessage message) {
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
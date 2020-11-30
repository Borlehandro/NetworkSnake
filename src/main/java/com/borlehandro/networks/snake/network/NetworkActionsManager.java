package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.game.repository.PlayersServersRepository;
import com.borlehandro.networks.snake.message_handlers.MessagesHandler;
import com.borlehandro.networks.snake.protocol.SendTask;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.MessageParser;
import com.borlehandro.networks.snake.protocol.messages.MessageType;
import com.borlehandro.networks.snake.protocol.messages.action.AckMessage;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;
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
    private final MessagesHandler handler;
    private static final SocketAddress MULTICAST_ADDRESS = new InetSocketAddress("239.192.0.4", 9192);

    private final PlayersServersRepository repository = PlayersServersRepository.getInstance();

    public NetworkActionsManager(MessagesHandler handler, int port) throws SocketException {
        this.handler = handler;
        // Todo fix port
        socket = new DatagramSocket(port);
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
                    // System.err.println("message Queue 50");
                    var sendTask = messagesQueue.pollFirst();
                    var messageJson = gson.toJson(sendTask.getMessage());
                    // System.err.println("in q: " + sendTask.getMessage().getType());
                    // Debug

                    // if (sendTask.getMessage().getType().equals(MessageType.STEER_MESSAGE))
                        // System.err.println("STEER_MESSAGE");
                    // if (sendTask.getMessage().getType().equals(MessageType.PING_MESSAGE))
                        // System.err.println("Send ping message");

                    var datagram = new DatagramPacket(
                            messageJson.getBytes(),
                            messageJson.length(),
                            sendTask.getReceiverAddress());
                    try {
                        socket.send(datagram);
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
            // Receive
            try {
                socket.setSoTimeout(100);
                byte[] datagramBuffer = new byte[2048]; // 2 Kb
                var receivedPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
                socket.receive(receivedPacket);
                String s = new String(datagramBuffer, 0, receivedPacket.getLength());
                Message message = MessageParser.parseMessage(s);
                // if (message.getType().equals(MessageType.PING_MESSAGE))
                    // System.err.println("Receive PING");
                // Todo Add response message in the queue
                if (message.getType().equals(MessageType.ACK_MESSAGE)) {
                    synchronized (waitResponseMessages) {
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
                                new AckMessage(message.getMessageNumber(), -1, message.getReceiverId()),
                                receivedPacket.getSocketAddress()
                        ));
                        // System.err.println("message Queue 120 off");
                    }
                }
                var socketAddress = new InetSocketAddress(receivedPacket.getAddress(), receivedPacket.getPort());
                handler.handleMessage(message, socketAddress);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException e) {
                e.printStackTrace();
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
}
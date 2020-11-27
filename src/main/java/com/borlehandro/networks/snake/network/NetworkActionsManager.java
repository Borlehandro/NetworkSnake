package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.message_handlers.MessagesHandler;
import com.borlehandro.networks.snake.protocol.SendTask;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.MessageParser;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Send directed messages to players
 */
public class NetworkActionsManager {
    private final DatagramSocket socket;
    private final ExecutorService networkTaskExecutor = Executors.newSingleThreadExecutor();
    private final ArrayDeque<SendTask> messagesQueue = new ArrayDeque<>();
    private final ArrayDeque<AnnouncementMessage> multicastQueue = new ArrayDeque<>();
    private final MessagesHandler handler;
    private static final SocketAddress MULTICAST_ADDRESS = new InetSocketAddress("239.192.0.4", 9192);

    public NetworkActionsManager(MessagesHandler handler, int port) throws SocketException {
        this.handler = handler;
        // Todo fix port
        socket = new DatagramSocket(port);
    }

    public void start() {
        networkTaskExecutor.execute(() -> {
            Gson gson = new Gson();
            while (!Thread.interrupted()) {
                // Send unicast
                synchronized (messagesQueue) {
                    if (!messagesQueue.isEmpty()) {
                        var sendTask = messagesQueue.pollFirst();
                        var messageJson = gson.toJson(sendTask.getMessage());
                        var datagram = new DatagramPacket(
                                messageJson.getBytes(),
                                messageJson.length(),
                                sendTask.getReceiverAddress());
                        try {
                            socket.send(datagram);
                            // Todo add in waitingForResponseQueue
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Send multicast
                synchronized (multicastQueue) {
                    if (!multicastQueue.isEmpty()) {
                        var message = multicastQueue.pollFirst();
                        var messageJson = gson.toJson(message);
                        var datagram = new DatagramPacket(messageJson.getBytes(),
                                messageJson.length(),
                                MULTICAST_ADDRESS);
                        try {
                            socket.send(datagram);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Receive
                try {
                    socket.setSoTimeout(500);
                    byte[] datagramBuffer = new byte[1024]; // 1 Kb
                    var receivedPacket = new DatagramPacket(datagramBuffer, datagramBuffer.length);
                    socket.receive(receivedPacket);
                    Message message = MessageParser.parseMessage(new String(datagramBuffer, 0, receivedPacket.getLength()));
                    // Todo send message to handler
                    // Todo Add response message in the queue
                    var socketAddress = new InetSocketAddress(receivedPacket.getAddress(), receivedPacket.getPort());
                    handler.handleMessage(message, socketAddress);
                } catch (SocketTimeoutException ignored) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void putSendTask(SendTask message) {
        synchronized (messagesQueue) {
            messagesQueue.addLast(message);
        }
    }

    public void putMulticast(AnnouncementMessage message) {
        synchronized (multicastQueue) {
            multicastQueue.addLast(message);
        }
    }
}
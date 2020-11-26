package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.PlayersRepository;
import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.MessageParser;
import com.google.gson.Gson;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Send directed messages to players
 */
public class NetworkActionsManager {
    private final DatagramSocket socket;
    private final ExecutorService networkTaskExecutor = Executors.newSingleThreadExecutor();
    private final ArrayDeque<Message> messagesQueue = new ArrayDeque<>();
    private final PlayersRepository repository = PlayersRepository.getInstance();
    private int masterId = -1;

    public NetworkActionsManager() throws SocketException {
        socket = new DatagramSocket(9192);
    }

    public void start() {
        networkTaskExecutor.execute(() -> {
            Gson gson = new Gson();
            while (!Thread.interrupted()) {
                // Send
                synchronized (messagesQueue) {
                    if (!messagesQueue.isEmpty()) {
                        var message = messagesQueue.pollFirst();
                        var messageJson = gson.toJson(message);
                        var datagram = new DatagramPacket(
                                messageJson.getBytes(),
                                messageJson.length(),
                                repository.findAddressById(message.getReceiverId()));
                        try {
                            socket.send(datagram);
                            // Todo add in waitingForResponseQueue
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
                    Message message = MessageParser.parseMessage(new String(datagramBuffer));
                    // Todo send message to handler
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
    }

    public int getMasterId() {
        return masterId;
    }

    public void putMessage(Message message) {
        synchronized (messagesQueue) {
            messagesQueue.addLast(message);
        }
    }

}

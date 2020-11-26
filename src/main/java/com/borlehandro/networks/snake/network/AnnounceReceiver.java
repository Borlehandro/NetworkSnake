package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.protocol.messages.Message;
import com.borlehandro.networks.snake.protocol.messages.factory.AnnouncementStateMessageFactory;
import com.borlehandro.networks.snake.protocol.messages.state.AnnouncementMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Send multicast messages
 */
public class AnnounceReceiver {
    private static final int SENDING_TIMEOUT_MILLIS = 1000;
    private final int senderId;
    private final MulticastSocket socket;
    private final InetAddress groupAddress;
    private final AnnouncementStateMessageFactory factory;
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();

    // TODO RECEIVE ANNOUNCES!
    public AnnounceReceiver(AnnouncementStateMessageFactory factory,
                            InetAddress networkInterfaceAddress,
                            int senderId) throws IOException {
        this.factory = factory;
        this.senderId = senderId;
        groupAddress = InetAddress.getByName("239.192.0.4");
        socket = new MulticastSocket(9192);
        socket.joinGroup(new InetSocketAddress(groupAddress, 9192), NetworkInterface.getByInetAddress(networkInterfaceAddress));
    }

    public void start() {
        sendExecutor.execute(() -> {
            Gson gson = new Gson();
            while (!Thread.interrupted()) {
                try {
                    byte[] buffer = new byte[1024]; // 1 Kb
                    var receivedDatagram = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivedDatagram);
                    Message message = gson.fromJson(new String(buffer), AnnouncementMessage.class);
                    // Todo send to announce handler
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
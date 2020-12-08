package com.borlehandro.networks.snake.network;

import com.borlehandro.networks.snake.message_handlers.MessagesHandler;
import com.borlehandro.networks.snake.protobuf.SnakesProto;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Receive multicast messages
 */
public class AnnounceReceiver extends Thread {
    private final MulticastSocket socket;
    private final InetAddress groupAddress;
    private final MessagesHandler messagesHandler;

    public AnnounceReceiver(MessagesHandler handler) throws IOException {
        groupAddress = InetAddress.getByName("239.192.0.4");
        socket = new MulticastSocket(9192);
        // Todo test network interface start
        // Todo use correct interface
        socket.joinGroup(new InetSocketAddress(groupAddress, 9192),
                NetworkInterface.getByInetAddress(InetAddress.getLocalHost())
        );
        this.messagesHandler = handler;
    }

    public void run() {
        while (!interrupted()) {
            try {
                byte[] buffer = new byte[2048]; // 2 Kb
                var receivedDatagram = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivedDatagram);
                // String s = new String(buffer, 0, receivedDatagram.getLength());
                System.err.println("Multicast receive: " + Arrays.toString(receivedDatagram.getData()));
                byte[] bytes = Arrays.copyOf(receivedDatagram.getData(), receivedDatagram.getLength());
                SnakesProto.GameMessage message = SnakesProto.GameMessage.parseFrom(bytes);
                var socketAddress = new InetSocketAddress(receivedDatagram.getAddress(), receivedDatagram.getPort());
                messagesHandler.handleMessage(message, socketAddress);
            } catch (IOException e) {
                e.printStackTrace();
                // Todo fix json exception
            } catch (JsonSyntaxException ignore) {
            }
        }
    }
}
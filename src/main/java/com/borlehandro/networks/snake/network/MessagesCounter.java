package com.borlehandro.networks.snake.network;

public class MessagesCounter {
    private static long number = 0;
    public static long next() {
        return number++;
    }
}

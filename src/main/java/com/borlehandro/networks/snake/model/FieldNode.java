package com.borlehandro.networks.snake.model;

import java.util.*;

public class FieldNode extends Node {

    Set<Snake> snakesOnTheNode = new LinkedHashSet<>();

    public enum State {
        WITH_FOOD, EMPTY, WITH_SNAKE_BODY, WITH_SNAKE_HEAD
    }

    private State state;

    public FieldNode(int x, int y) {
        super(x, y);
        this.state = State.EMPTY;
    }

    public boolean isOccupiedOnlyByThisOne(Snake s) {
        return snakesOnTheNode.size() == 1 && snakesOnTheNode.contains(s);
    }

    public void addSnake(Snake s) {
        snakesOnTheNode.add(s);
    }

    public void removeSnake(Snake s) {
        snakesOnTheNode.remove(s);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}

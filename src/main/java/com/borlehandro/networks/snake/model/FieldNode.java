package com.borlehandro.networks.snake.model;

import java.util.*;

public class FieldNode extends Node {

    Set<Snake> bodiesOnTheNode = new LinkedHashSet<>();
    Set<Snake> headsOnTheNode = new LinkedHashSet<>();

    public enum State {
        WITH_FOOD, EMPTY, WITH_SNAKE_BODY, WITH_SNAKE_HEAD
    }

    private State state;

    public FieldNode(int x, int y) {
        super(x, y);
        this.state = State.EMPTY;
    }

    public boolean isOccupiedOnlyByThisOne(Snake s) {
        return (bodiesOnTheNode.size() == 1 && bodiesOnTheNode.contains(s))
                ^ (headsOnTheNode.size() == 1 && headsOnTheNode.contains(s));
    }

    public void addSnake(Snake s, boolean isHead) {
        if (isHead) {
            state = State.WITH_SNAKE_HEAD;
            headsOnTheNode.add(s);
        } else {
            state = State.WITH_SNAKE_BODY;
            bodiesOnTheNode.add(s);
        }
    }

    public void removeSnake(Snake s, boolean isHead) {
        if (isHead)
            headsOnTheNode.remove(s);
        else
            bodiesOnTheNode.remove(s);
    }

    public void removeSnake(Snake s) {
        headsOnTheNode.remove(s);
        bodiesOnTheNode.remove(s);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Set<Snake> getBodiesOnTheNode() {
        return bodiesOnTheNode;
    }

    public Set<Snake> getHeadsOnTheNode() {
        return headsOnTheNode;
    }
}

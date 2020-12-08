package com.borlehandro.networks.snake.model;

import com.borlehandro.networks.snake.protobuf.SnakesProto;

import java.util.*;
import java.util.stream.Collectors;

public class Snake {

    private boolean hadFood = false;
    private SnakeNode lastTail;
    private SnakeState state;
    private int playerId;

    public int getPlayerId() {
        return playerId;
    }

    public enum SnakeState {
        ALIVE,
        ZOMBIE
    }

    public Snake(int playerId) {
        this.playerId = playerId;
        state = SnakeState.ALIVE;
    }

    public void setZombie() {
        state = SnakeState.ZOMBIE;
    }

    public SnakeState getState() {
        return state;
    }

    public Direction getHeadDirection() {
        return body.getFirst().getNodeDirection();
    }

    public ArrayDeque<SnakeNode> getBody() {
        return body;
    }

    public LinkedHashMap<Node, Direction> getChangeDirectionNode() {
        return changeDirectionNode;
    }

    public void addNode(int x, int y, Direction direction) {
        body.addLast(new SnakeNode(x, y, direction));
    }

    public boolean hadFood() {
        return hadFood;
    }

    public void setHadFood(boolean hadFood) {
        this.hadFood = hadFood;
    }

    public void changePlayerId(int newId) {
        playerId = newId;
    }

    public SnakeNode getLastTail() {
        return lastTail;
    }

    /**
     * Copy and save values of previous tail
     * @param previousTail Previous tail node
     */
    public void setLastTail(SnakeNode previousTail) {
        this.lastTail = new SnakeNode(previousTail.x, previousTail.y, previousTail.nodeDirection);
    }

    public void clearLastTail() {
        lastTail = null;
    }

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private final ArrayDeque<SnakeNode> body = new ArrayDeque<>();
    private LinkedHashMap<Node, Direction> changeDirectionNode = new LinkedHashMap<>();

    public static class SnakeNode extends Node {

        private Direction nodeDirection;

        protected SnakeNode(int x, int y, Direction nodeDirection) {
            super(x, y);
            this.nodeDirection = nodeDirection;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Direction getNodeDirection() {
            return nodeDirection;
        }

        public void setNodeDirection(Direction newDirection) {
            this.nodeDirection = newDirection;
        }

        @Override
        public String toString() {
            return "SnakeNode{" +
                    "nodeDirection=" + nodeDirection +
                    ", x=" + x +
                    ", y=" + y +
                    '}';
        }
    }
    // Todo test
    public static Snake ofProtoSnake(SnakesProto.GameState.Snake protoSnake) {
        Snake s = new Snake(protoSnake.getPlayerId());
        // It's fake direction!
        s.body.addAll(protoSnake.getPointsList().stream().map(coord ->
                new SnakeNode(coord.getX(), coord.getY(), Direction.UP)).collect(Collectors.toList()));
        s.state = switch (protoSnake.getState()) {
            case ALIVE -> SnakeState.ALIVE;
            case ZOMBIE -> SnakeState.ZOMBIE;
        };
        return s;
    }
}
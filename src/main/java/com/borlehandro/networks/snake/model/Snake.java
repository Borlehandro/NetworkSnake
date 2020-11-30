package com.borlehandro.networks.snake.model;

import com.borlehandro.networks.snake.protocol.Coordinates;

import java.util.*;

public class Snake {

    private boolean hadFood = false;
    private SnakeNode lastTail;
    private SnakeState state;
    public enum SnakeState {
        ALIVE,
        ZOMBIE
    }

    public Snake() {
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

    private ArrayDeque<SnakeNode> body = new ArrayDeque<>();
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

        public Coordinates getCoordinates() {
            return new Coordinates(x, y);
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

    public void setChangeDirectionNode(LinkedHashMap<Node, Direction> changeDirectionNode) {
        this.changeDirectionNode = changeDirectionNode;
    }
}

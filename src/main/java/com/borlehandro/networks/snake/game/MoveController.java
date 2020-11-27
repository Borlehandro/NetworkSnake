package com.borlehandro.networks.snake.game;

import com.borlehandro.networks.snake.model.Field;
import com.borlehandro.networks.snake.model.FieldNode;
import com.borlehandro.networks.snake.model.Node;
import com.borlehandro.networks.snake.model.Snake;

public class MoveController {

    private final Field field;

    public MoveController(Field field) {
        this.field = field;
    }

    /**
     * Rotate snake
     * @return false if can not rotate or Exception, true if rotation successful
     */
    public boolean rotate(Snake s, Snake.Direction direction) {
        Snake.SnakeNode head = s.getBody().peekFirst();
        if (canRotate(head.getNodeDirection(), direction)) {
            head.setNodeDirection(direction);
            s.getChangeDirectionNode().put(new Node(head.getX(), head.getY()), direction);
            return true;
        } else {
            return false;
        }
    }

    private boolean canRotate(Snake.Direction from, Snake.Direction to) {
        return !(from == Snake.Direction.DOWN && to == Snake.Direction.UP)
                && !(from == Snake.Direction.UP && to == Snake.Direction.DOWN)
                && !(from == Snake.Direction.LEFT && to == Snake.Direction.RIGHT)
                && !(from == Snake.Direction.RIGHT && to == Snake.Direction.LEFT);
    }

    public void moveForward(Snake s) {
        var body = s.getBody();
        changeBodyDirections(s);
        // TODO Test
        for (Snake.SnakeNode snakeNode : body) {
            moveNodeForward(snakeNode, s);
        }
        if (s.hadFood()) {
            if(s.getBody().size() == field.getFieldMatrix().length - 1) {
                System.out.println("WARN!");
            }
            int newTailX = s.getLastTail().getX();
            int newTailY = s.getLastTail().getY();
            s.addNode(newTailX, newTailY, s.getLastTail().getNodeDirection());
            field.getFieldMatrix()[newTailX][newTailY].addSnake(s, false);
            s.clearLastTail();
            s.setHadFood(false);
        }
    }

    private void changeBodyDirections(Snake s) {
        var body = s.getBody();
        var changeDirectionNodes = s.getChangeDirectionNode();
        var iterator = changeDirectionNodes.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var node = entry.getKey();
            var newDirection = entry.getValue();
            var opt = body.stream().filter(snakeNode -> snakeNode.hasEqualsCoordinates(node)).findFirst();
            opt.ifPresent(snakeNode -> {
                snakeNode.setNodeDirection(newDirection);
                if (snakeNode.equals(body.getLast())) {
                    // TODO Test this dangerous moment
                    iterator.remove();
                }
            });
        }
    }

    // TODO Refactor moving
    public void moveNodeForward(Snake.SnakeNode snakeNode, Snake s) {
        // Clear previous node
        var fieldNode = field.getFieldMatrix()[snakeNode.getX()][snakeNode.getY()];
        if (fieldNode.isOccupiedOnlyByThisOne(s)) {
            fieldNode.setState(FieldNode.State.EMPTY);
        }
        boolean isHead = s.getBody().getFirst().equals(snakeNode);
        fieldNode.removeSnake(s, isHead);
        int movedX, movedY;
        switch (snakeNode.getNodeDirection()) {
            case DOWN -> {
                movedX = snakeNode.getX();
                movedY = snakeNode.getY() - 1;
            }
            case UP -> {
                movedX = snakeNode.getX();
                movedY = snakeNode.getY() + 1;
            }
            case LEFT -> {
                movedX = snakeNode.getX() - 1;
                movedY = snakeNode.getY();
            }
            case RIGHT -> {
                movedX = snakeNode.getX() + 1;
                movedY = snakeNode.getY();
            }
            default -> throw new IllegalArgumentException("Wrong direction value");
        }
        movedX = field.toLoop(movedX, Field.Axis.X);
        movedY = field.toLoop(movedY, Field.Axis.Y);

        var newFieldNode = field.getFieldMatrix()[movedX][movedY];
        if(newFieldNode.getState().equals(FieldNode.State.WITH_FOOD)) {
            field.decrementFood();
            s.setHadFood(true);
            s.setLastTail(s.getBody().getLast());
        }
        newFieldNode.addSnake(s, isHead);
        snakeNode.setX(movedX);
        snakeNode.setY(movedY);
    }
}
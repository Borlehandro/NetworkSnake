package com.borlehandro.networks.snake.game_controll;

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
            int newTailX = s.getLastTail().getX();
            int newTailY = s.getLastTail().getY();
            s.addNode(newTailX, newTailY, s.getLastTail().getNodeDirection());
            field.getFieldMatrix()[newTailX][newTailY].setState(FieldNode.State.WITH_SNAKE_BODY);
            field.getFieldMatrix()[newTailX][newTailY].addSnake(s);
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
            fieldNode.removeSnake(s);
        }
        boolean isHead = s.getBody().getFirst().equals(snakeNode);
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
        movedX = field.toLoop(movedX);
        movedY = field.toLoop(movedY);

        var newFieldNode = field.getFieldMatrix()[movedX][movedY];
        if(newFieldNode.getState().equals(FieldNode.State.WITH_FOOD)) {
            s.setHadFood(true);
            s.setLastTail(s.getBody().getLast());
        }
        newFieldNode.setState(isHead ? FieldNode.State.WITH_SNAKE_HEAD : FieldNode.State.WITH_SNAKE_BODY);
        newFieldNode.addSnake(s);
        snakeNode.setX(movedX);
        snakeNode.setY(movedY);
    }
}
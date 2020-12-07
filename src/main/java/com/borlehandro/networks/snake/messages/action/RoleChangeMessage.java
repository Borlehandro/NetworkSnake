package com.borlehandro.networks.snake.messages.action;

import com.borlehandro.networks.snake.model.NodeRole;
import com.borlehandro.networks.snake.messages.MessageType;

public class RoleChangeMessage extends ActionMessage {
    private final NodeRole senderRole;
    private final NodeRole receiverRole;

    public RoleChangeMessage(NodeRole senderRole, NodeRole receiverRole, long messageNumber, int senderId, int receiverId) {
        super(messageNumber, senderId, receiverId);
        this.senderRole = senderRole;
        this.receiverRole = receiverRole;
        type = MessageType.ROLE_CHANGE_MESSAGE;
    }

    public NodeRole getSenderRole() {
        return senderRole;
    }

    public NodeRole getReceiverRole() {
        return receiverRole;
    }
}

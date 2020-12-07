package com.borlehandro.networks.snake.messages;

import com.borlehandro.networks.snake.messages.action.*;
import com.borlehandro.networks.snake.messages.state.AnnouncementMessage;
import com.borlehandro.networks.snake.messages.state.GameStateMessage;
import com.google.gson.Gson;

public class MessageParser {
    public static Message parseMessage(String json) {
        Gson gson = new Gson();
        var marker = gson.fromJson(json, MessageMarker.class);
        Class<? extends Message> messageClass = switch (marker.getType()) {
            case PING_MESSAGE -> PingMessage.class;
            case ACK_MESSAGE -> AckMessage.class;
            case JOIN_MESSAGE -> JoinMessage.class;
            case ERROR_MESSAGE -> ErrorMessage.class;
            case STEER_MESSAGE -> SteerMessage.class;
            case GAME_STATE_MESSAGE -> GameStateMessage.class;
            case ROLE_CHANGE_MESSAGE -> RoleChangeMessage.class;
            case ANNOUNCEMENT_MESSAGE -> AnnouncementMessage.class;
        };
        return gson.fromJson(json, messageClass);
    }
}

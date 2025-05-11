package site.code4fun.config.websocket;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

public class BinaryHandler extends BinaryWebSocketHandler { // TODO implement to transfer file

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] payload = message.getPayload().array();
        System.out.println("Received binary message: " + new String(payload));
        session.sendMessage(new BinaryMessage(payload));
    }
}


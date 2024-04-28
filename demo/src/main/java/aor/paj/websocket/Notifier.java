package aor.paj.websocket;

import aor.paj.bean.MessageBean;
import aor.paj.websocket.bean.HandleWebSockets;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.HashMap;
import java.util.Map;

@Singleton
@ServerEndpoint("/websocket/notifier/{token}")

public class Notifier {

    @Inject
    private HandleWebSockets handleWebSockets;


    private HashMap<String, Session> sessions = new HashMap<>();

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocket session is opened for client with token: " + token);
        sessions.put(token, session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session) sessions.remove(key);
        }
    }

    @OnMessage
    public void toDoOnMessage(Session session, String json) {
        try {
            handleWebSockets.handleWebSocketJSON(session, json);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        }
    }

    public Map<String, Session> getSessions() {
        return this.sessions;
    }

    public void sendToAllSessions(String messageJson) {
        for (Session session : sessions.values()) {
            try {
                session.getBasicRemote().sendObject(messageJson);
            } catch (Exception e) {
                System.out.println("Something went wrong!");
            }
        }
    }

    public void sendToAllProductOwnerSessions(String messageJson) {
        for (Session session : sessions.values()) {
            try {
                String token = session.getPathParameters().get("token");
                if(handleWebSockets.isProductOwner(token)){
                    session.getBasicRemote().sendObject(messageJson);
                }

            } catch (Exception e) {
                System.out.println("Something went wrong!");
            }
        }
    }
}

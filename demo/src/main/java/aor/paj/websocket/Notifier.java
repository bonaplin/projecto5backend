package aor.paj.websocket;

import aor.paj.bean.MessageBean;
import aor.paj.websocket.bean.HandleWebSockets;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.HashMap;
import java.util.Map;

@Singleton
@ServerEndpoint("/websocket/notifier/{token}")

public class Notifier {

    @EJB
    private HandleWebSockets handleWebSockets;

    @EJB
    private MessageBean messageBean;

    private HashMap<String, Session> sessions = new HashMap<>();

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token){
        System.out.println("A new WebSocket session is opened for client with token: "+ token);
        sessions.put(token,session);
    }
    @OnClose
    public void toDoOnClose(Session session, CloseReason reason){
        System.out.println("Websocket session is closed with CloseCode: "+
                reason.getCloseCode() + ": "+reason.getReasonPhrase());
        for(String key:sessions.keySet()){
            if(sessions.get(key) == session) sessions.remove(key);
        }
    }
    @OnMessage
    public void toDoOnMessage(Session session, String json){
        try {
            handleWebSockets.handleWebSocketJSON(session, json);
//            MessageSocketDto message = messageBean.jsonToMessageSocketDto(json);
//            String userToken = message.getSenderToken();
//            String receiver = message.getReceiverUsername();
//            String messageToSend = messageBean.getMessageToSend(message);
//            System.out.println(message);
//            session.getBasicRemote().sendObject(messageToSend);
//            messageBean.sendToUser(receiver, messageToSend);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong!");
        }
    }
    public Map<String, Session> getSessions() {
        return this.sessions;
    }

    }
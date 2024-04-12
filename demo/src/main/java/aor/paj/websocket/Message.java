package aor.paj.websocket;

import aor.paj.dao.TokenDao;
import aor.paj.dao.UserDao;
import aor.paj.dto.MessageSocketDto;
import com.google.gson.Gson;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Singleton
@ServerEndpoint("/websocket/message/{token}")

public class Message {
    @EJB
    private UserDao userDao;
    @EJB
    private TokenDao tokenDao;

    private HashMap<String, Session> sessions = new HashMap<>();
    private Gson gson = new Gson();


    public void send(String token, String msg){
        Session session = sessions.get(token);
        if (session != null){
            System.out.println("sending.......... "+msg);
            try {
                session.getBasicRemote().sendText(msg);
            }
            catch (IOException e) {
                System.out.println("Something went wrong!"); }
        }
    }

    public void sendToUser(String receiver, String message){

        // Obter o ID do usuário com o nome de usuário fornecido
        int userId = userDao.findUserByUsername(receiver).getId();

        // Obter a lista de tokens que pertencem a esse ID de usuário
        List<String> userTokens = tokenDao.findTokensByUserId(userId);

        // Para cada token, obter a sessão correspondente e enviar a mensagem
        for (String token : userTokens) {
            Session session = sessions.get(token);

            if (session != null) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.out.println("Something went wrong!");
                }
            }
        }
    }
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
    public void toDoOnMessage(Session session, String msg){
        try {
            MessageSocketDto message = jsonToMessageSocketDto(msg);

            String userToken = "token user:" + message.getSenderToken();
            String receiverToken = "token user:" + message.getReceiverUsername();
            String messageToSend = gson.toJson(message);

            System.out.println(message);
            session.getBasicRemote().sendText(userToken+" "+messageToSend+" "+receiverToken+" "+messageToSend);
        }
        catch (IOException e) {
            System.out.println("Something went wrong!");
        }
    }

    public HashMap<String, Session> getSessions() {
        return sessions;
    }

    public MessageSocketDto jsonToMessageSocketDto(String json){
        MessageSocketDto msg = null;
        try{
           msg = gson.fromJson(json, MessageSocketDto.class);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Error in converting json to MessageSocketDto");
            //tratar o erro...
        }
        return msg;
    }
}
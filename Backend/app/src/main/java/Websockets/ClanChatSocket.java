package Websockets;


import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;


@Controller
@ServerEndpoint(value = "/chat/clan/{username}")
public class ClanChatSocket {

    private static ClanChatMessageRepository CCMRepo;



    @Autowired
    public void setCCMRepo(ClanChatMessageRepository repo) {
        CCMRepo = repo;
    }



    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();

    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(ClanChatSocket.class);


    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException {
        logger.info("Entered open");
        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);
        sendMessageToParticularUser(username, getChatHistory());
        String message = username + " has joined the clan chat! Welcome them to the clan!";
        broadcast(message);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        logger.info("Entered into Message: Got Message: " + message);
        String username = sessionUsernameMap.get(session);

        broadcast(username + ": " + message);

        CCMRepo.save(new ClanChatMessage(username, message));
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        logger.info("Closing");

        String username = sessionUsernameMap.get(session);

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);

        String message = username + " left the clan chat.";
        broadcast(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.info("Entered error");
        throwable.printStackTrace();
    }

    public void sendMessageToParticularUser(String username, String message) {
        try {
            usernameSessionMap.get(username).getBasicRemote().sendText(message);
        }
        catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }

    private void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            }
            catch (IOException e) {
                logger.info(("Exception: " + e.getMessage().toString()));
                e.printStackTrace();
            }
        });
    }

    private String getChatHistory() {
        List<ClanChatMessage> messages = CCMRepo.findAll();

        StringBuilder sb = new StringBuilder();
        if (messages != null && messages.size() != 0) {
            for (ClanChatMessage message : messages) {
                sb.append(message.getUserName() + ": " + message.getContent() + "\n");
            }
        }
        return sb.toString();
    }


}

package models.services;

import models.IMessageObserver;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lenovo on 23.07.2017.
 */
@javax.websocket.ClientEndpoint
public class ClientEndpoint {


    private static String roomName = "test";
    private Session session;
    private WebSocketContainer container;
    private List<IMessageObserver> observerList = new ArrayList<>();

    public ClientEndpoint() {
        container = ContainerProvider.getWebSocketContainer();
    }


    public void init(){
        URI uri = URI.create("ws://localhost:8080/live/" + "test");
        try {
            container.connectToServer(this, uri);
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @OnOpen
    public void onOpen(Session session){
      this.session = session;
      System.out.println("Połączono z socketem");
    }

    @OnClose
    public void onClose(){

    }

    @OnMessage
    public void onMessage(Session session, ByteBuffer byteBuffer) {
        notifyObservers(byteBuffer);
    }

    public Session getSession() {
        return session;
    }


    public WebSocketContainer getContainer() {
        return container;
    }



    public void registerObserver(IMessageObserver observer) {
        observerList.add(observer);
    }

    private void notifyObservers(ByteBuffer buffer){
        observerList.forEach(s -> s.onNewMessage(buffer));
    }
}

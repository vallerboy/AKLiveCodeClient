package pl.oskarpolak.models.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import pl.oskarpolak.models.IMessageObserver;
import pl.oskarpolak.models.ISocketConnection;
import pl.oskarpolak.models.MessageModel;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lenovo on 23.07.2017.
 */
@javax.websocket.ClientEndpoint
public class ClientEndpoint {


    private static ClientEndpoint endpoint = new ClientEndpoint();
    public static ClientEndpoint getConnection(){
        return endpoint;
    }

    private static final Gson gson = new GsonBuilder().create();


    private String roomName;
    private Session session;
    private WebSocketContainer container;
    private List<IMessageObserver> observerList = new ArrayList<>();

    private ExecutorService executorService;

    private ClientEndpoint() {
        container = ContainerProvider.getWebSocketContainer();
        System.out.println("Znalazlem provider");
    }

    private ISocketConnection connector;

    public void init(String roomName, ISocketConnection connector){
        this.roomName = roomName;
        this.connector = connector;
        executorService = Executors.newSingleThreadExecutor();
        runConnectionAsyncTask();
    }


    private void runConnectionAsyncTask() {
        System.out.println("Lacze z socketem");
        executorService.execute(() -> {
            System.out.println("Lacze z socketem");
            URI uri = URI.create("ws://localhost:8080/live/" + roomName);
            try {
                container.connectToServer(this, uri);
            } catch (DeploymentException | IOException e) {
                    connector.onError();
            }
            System.out.println("Polaczono");


            Platform.runLater(() -> connector.onConnect());
        });
    }

    public void sendMessage(MessageModel model) {
        String message = gson.toJson(model);
        try {
            endpoint.getSession().getBasicRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
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
        observerList.forEach(s -> {
            Type type = new TypeToken<MessageModel>() {}.getType();
            MessageModel messageModel = gson.fromJson(new String(buffer.array()), type);
            s.onNewMessage(messageModel);
        });
        executorService.shutdownNow();
    }
}

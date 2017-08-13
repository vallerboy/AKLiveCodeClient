package pl.oskarpolak.models;

/**
 * Created by Lenovo on 23.07.2017.
 */
public interface IMessageObserver {
    void onNewMessage(MessageModel model);
}

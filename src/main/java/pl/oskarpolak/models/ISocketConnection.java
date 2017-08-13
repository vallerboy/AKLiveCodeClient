package pl.oskarpolak.models;

/**
 * Created by Lenovo on 13.08.2017.
 */
public interface ISocketConnection {
    void onConnect();
    void onError();
}

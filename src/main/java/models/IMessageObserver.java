package models;

import java.io.BufferedReader;
import java.nio.ByteBuffer;

/**
 * Created by Lenovo on 23.07.2017.
 */
public interface IMessageObserver {
    void onNewMessage(ByteBuffer byteBuffer);
}

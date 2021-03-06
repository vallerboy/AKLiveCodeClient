package pl.oskarpolak.models;

/**
 * Created by Lenovo on 23.07.2017.
 */
public class MessageModel {
    public enum MessageType{
        REGISTER, DISCONNECT, DOWNLOAD_REQUEST, UPDATING, DOWNLOAD_RESPONSE, JOIN, REQUEST_ALL_USER, REGISTER_RESPONSE, UPDATE_ALL;
    }

    private String toWho;
    private String context;
    private MessageType messageType;




    public String getToWho() {
        return toWho;
    }

    public void setToWho(String nickname) {
        this.toWho = nickname;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}

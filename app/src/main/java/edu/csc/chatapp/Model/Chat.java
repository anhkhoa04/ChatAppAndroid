package edu.csc.chatapp.Model;

public class Chat {

    private String time;
    private String sender;
    private String receiver;
    private String message;
    private String seen;

    public Chat(String time,String sender, String receiver, String message, String seen) {
        this.time = time;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.seen = seen;
    }

    public Chat() {

    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}

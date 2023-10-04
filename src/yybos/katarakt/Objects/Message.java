package yybos.katarakt.Objects;

/*
*   The message will run through json
*   Ex: {
*       "message": "message bla bla bla",
*       "type": "Message"
*       "size": "19"
*   /0}
*/

import com.google.gson.Gson;

import java.sql.Date;

public class Message {
    private int id;
    private Type type;

    private String message;
    private int size;
    private Date dt;
    private int chat;
    private int user;

    public int getId () {
        return id;
    }
    public Type getType () {
        return type;
    }
    public String getMessage () {
        return message;
    }
    public int getSize () {
        return size;
    }
    public Date getDate () {
        return dt;
    }
    public int getChat () {
        return chat;
    }
    public int getUser () {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setType(int type) {
        this.type = Type.getEnumByValue(type);
    }
    public void setMessage (String message) {
        this.message = message;
        this.size = message.length();
    }
    public void setDate(Date dt) {
        this.dt = dt;
    }
    public void setChat(int chat) {
        this.chat = chat;
    }
    public void setUser(int user) {
        this.user = user;
    }

    public static Message toMessage (Message.Type type, String message, int chat, int user) {
        Message from = new Message();
        from.type = type;
        from.message = message;
        from.size = message.length();
        from.chat = chat;
        from.user = user;

        return from;
    }
    public static Message fromString (String json) {
        Gson messageParser = new Gson();

        return messageParser.fromJson(json, Message.class);
    }

    public enum Type {
        Message(0),
        Command(1),
        Version(2);

        private final int value;

        Type (int value) {
            this.value = value;
        }

        public int getValue () {
            return value;
        }
        public static Type getEnumByValue (int value) {
            for (Type enumValue : Type.values()) {
                if (enumValue.getValue() == value) {
                    return enumValue;
                }
            }
            throw new IllegalArgumentException("No type with value " + value);
        }
    }

    @Override
    public String toString() {
        return "{\nid: " + this.id +
                ",\ntype: '" + this.type +
                "',\nmessage: '" + this.message +
                "',\nsize: " + this.size +
                ",\ndate: " + this.dt +
                ",\nchat: " + this.chat +
                ",\nuser: " + this.user + "\n}\n";
    }
}

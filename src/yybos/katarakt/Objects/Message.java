package yybos.katarakt.Objects;

/*
 *   The message will run through json
 *   Ex: {
 *       "message": "message bla bla bla",
 *       "type": "Message"
 *       "size": "19"
 *   }/0
 */


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Date;

public class Message extends PacketObject {
    private final User user = new User();

    public Message () {
        super.setType(PacketObject.Type.Message.getValue());
    }

    public int getId () {
        return this.id;
    }
    public String getMessage () {
        return this.e;
    }
    public int getChat () {
        return this.a;
    }
    public String getUsername () {
        return this.user.getUsername();
    }
    public int getUserId () {
        return this.user.getId();
    }
    public Date getDate () {
        return this.date;
    }

    public void setMessage (String message) {
        this.e = message;
    }
    public void setChat (int id) {
        this.a = id;
    }
    public void setUsername (String username) {
        this.user.setUsername(username);
    }
    public void setUserId (int id) {
        this.user.setId(id);
    }

    public static Message toMessage (String message, int chat, String username, int userId) {
        Message from = new Message();
        from.e = message;
        from.a = chat;
        from.user.setUsername(username);
        from.user.setId(userId);
        from.date = new Date(System.currentTimeMillis());

        return from;
    }
    public static Message toMessage (String message, String username) {
        Message from = new Message();
        from.e = message;
        from.a = 0;
        from.user.setUsername(username);
        from.b = 0;
        from.date = new Date(System.currentTimeMillis());

        return from;
    }
    public static Message fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Message.class);
    }

    @Override
    public String toString() {
        return "{\nid: " + this.id +
                ",\ntype: '" + this.type +
                "',\nmessage: '" + this.e +
                ",\ndate: " + this.date +
                ",\nchat: " + this.a +
                ",\nuser: " + this.b + "\n}\n";
    }
}

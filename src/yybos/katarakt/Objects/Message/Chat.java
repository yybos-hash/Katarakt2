package yybos.katarakt.Objects.Message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import yybos.katarakt.Objects.ObjectDateDeserializer;
import yybos.katarakt.Objects.PacketObject;

import java.sql.Date;
import java.sql.Timestamp;

public class Chat extends PacketObject {
    private String name;
    private int userId;

    public Chat () {
        super.setType(PacketObject.Type.Chat.getValue());
    }

    public void setName (String name) {
        this.name = name;
    }
    public void setUser (int id) {
        this.userId = id;
    }

    public String getName () {
        return this.name;
    }

    public static Chat toChat (int id, int user, String name) {
        Chat chat = new Chat();
        chat.id = id;
        chat.userId = user;
        chat.name = name;

        return chat;
    }
    public static Chat toChat (int id, int user, long date, String name) {
        Chat chat = new Chat();
        chat.id = id;
        chat.userId = user;
        chat.name = name;
        chat.date = date;

        return chat;
    }
    public static Chat fromString (String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Date.class, new ObjectDateDeserializer());
        //  Basically when gson formats a Date in the sql.Date format it changes the format, so this keeps the it as it should

        Gson parser = gsonBuilder.serializeNulls().create();

        return parser.fromJson(json, Chat.class);
    }

    @Override
    public String toString () {
        return "id: " + this.id + "\n" + "name: " + this.name + "\n" + "date: " + this.date;
    }
}


